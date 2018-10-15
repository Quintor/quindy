package nl.quintor.studybits.indy.wrapper;


import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.dto.Verinym;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import nl.quintor.studybits.indy.wrapper.util.SeedUtil;
import org.hyperledger.indy.sdk.pool.Pool;
import org.junit.Test;

import static nl.quintor.studybits.indy.wrapper.TestUtil.removeIndyClientDirectory;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class WalletMessagesIT {

    @Test
    public void WalletMessagesTest() throws Exception {
        // Clear indy_client directory
        removeIndyClientDirectory();

        // Set pool protocol version based on PoolUtils
        Pool.setProtocolVersion(PoolUtils.PROTOCOL_VERSION).get();

        // Initialize message types for MessageEnvelope
        IndyMessageTypes.init();

        // Create and open Pool with 'default_pool' as pool name
        String poolName = PoolUtils.createPoolLedgerConfig(null, "testPool" + System.currentTimeMillis());
        IndyPool indyPool = new IndyPool(poolName);


        // Create new wallet for Steward
        TrustAnchor steward = new TrustAnchor(IndyWallet.create(indyPool, "steward", "000000000000000000000000Steward1"));
        // Create new wallet for faber
        Issuer faber = new Issuer(IndyWallet.create(indyPool, "faber", SeedUtil.generateSeed()));
        // Create a new wallet for alice
        Prover alice = new Prover(IndyWallet.create(indyPool, "alice", SeedUtil.generateSeed()), "alice_master_secret");


        // Onboard faber
        // Steward creates a connection request for faber with the TRUST_ANCHOR ROLE
        ConnectionRequest connectionRequestForFaber = steward.createConnectionRequest(faber.getName(), "TRUST_ANCHOR").get();
        // Steward creates a message ready to send to faber
        String connectionRequestForFaberMessage = steward.createMessage(connectionRequestForFaber).get().toJSON();
        System.out.println("Connection request message:");
        System.out.println(connectionRequestForFaberMessage);

        // Faber receives the connection request
        // Faber decrypts the message to get the ConnectionRequest object
        ConnectionRequest connectionRequestForFaberReceived = faber.decryptMessage(connectionRequestForFaberMessage);

        // Faber accepts the connectionRequest and creates a connectionResponse
        ConnectionResponse connectionResponseForSteward = faber.acceptConnectionRequest(connectionRequestForFaberReceived).get();
        // Faber encrypts the message ready to send back to steward
        String faberConnectionResponseMessage =  faber.createMessage(connectionResponseForSteward).get().toJSON();
        System.out.println("Connection response message:");
        System.out.println(faberConnectionResponseMessage);

        // Steward receives the message from Faber and decrypts it
        ConnectionResponse connectionResponseForStewardReceived = steward.decryptMessage(faberConnectionResponseMessage);
        // Steward accepts the connection response received from Faber
        steward.acceptConnectionResponse(connectionResponseForStewardReceived).get();


        // Faber creates a verinymRequest
        Verinym verinymRequest = faber.createVerinymRequest(connectionRequestForFaber.getDid());
        // Faber creates and encrypts the message ready to send
        String verinymRequestMessage = faber.createMessage(verinymRequest).get().toJSON();
        System.out.println("Verinym request message:");
        System.out.println(verinymRequestMessage);

        // Steward accepts verinym request from Faber and thus writes the new DID on the ledger
        Verinym verinymRequestReceived = steward.decryptMessage(verinymRequestMessage);
        steward.acceptVerinymRequest(verinymRequestReceived).get();

        // Assert that the connection request received by faber is equally to the one sent by steward
        assertThat(connectionRequestForFaberReceived.getDid(), is(equalTo(connectionRequestForFaber.getDid())));
        assertThat(connectionRequestForFaberReceived.getRequestNonce(), is(equalTo(connectionRequestForFaber.getRequestNonce())));
        // Assert that the connection response received by steward is equally to the one sent by faber
        assertThat(connectionResponseForSteward.getDid(), is(equalTo(connectionResponseForStewardReceived.getDid())));
        assertThat(connectionResponseForSteward.getRequestNonce(), is(equalTo(connectionResponseForStewardReceived.getRequestNonce())));
        // Assert that verinym request received by stward is equally to the one sent by faber
        assertThat(verinymRequest.getDid(), is(equalTo(verinymRequestReceived.getDid())));
        assertThat(verinymRequest.getVerkey(), is(equalTo(verinymRequestReceived.getVerkey())));

        // Now faber can onboard Alice
        // Faber creates a connection request message for alice
        String connectionRequestForAliceMessage = faber.createMessage(faber.createConnectionRequest(alice.getName(), null).get()).get().toJSON();

        // Alice receives and decrypts the message
        ConnectionRequest connectionRequestForAliceReceived = alice.decryptMessage(connectionRequestForAliceMessage);
        // Alice accepts the connection request and creates a connection response message for Faber
        String connectionResponseForFaberMessage =  alice.createMessage(alice.acceptConnectionRequest(connectionRequestForAliceReceived).get()).get().toJSON();

        // Faber receives and decrypts the response from alice
        ConnectionResponse connectionResponseForFaberReceived = faber.decryptMessage(connectionResponseForFaberMessage);
        // Faber accepts the response
        String aliceFaberDid = faber.acceptConnectionResponse(connectionResponseForFaberReceived).get();
        System.out.println("New DID for Alice-Faber: " + aliceFaberDid);
    }
}
