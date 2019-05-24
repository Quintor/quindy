package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import org.apache.commons.io.FileUtils;
import org.hyperledger.indy.sdk.IndyException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes.*;

public class TestUtil {
    public static void removeIndyClientDirectory() throws Exception {
        String homeDir = System.getProperty("user.home");
        File indyClientDir = Paths.get(homeDir, ".indy_client")
                .toFile();
        FileUtils.deleteDirectory(indyClientDir);
    }

    public static void onboardIssuer(TrustAnchor steward, Issuer newcomer) throws InterruptedException, ExecutionException, IndyException, IOException {
        // Create Codecs to facilitate encryption/decryption
        MessageEnvelopeCodec stewardCodec = new MessageEnvelopeCodec(steward);
        MessageEnvelopeCodec newcomerCodec = new MessageEnvelopeCodec(newcomer);

        // Connecting newcomer with Steward

        // We revert the order from the tutorial, since we use the anoncryption from the verinym

        // Create connection request for steward
        String connectionRequestString = newcomerCodec.encryptMessage(newcomer.createConnectionRequest().get(),
                IndyMessageTypes.CONNECTION_REQUEST, steward.getMainDid()).get().toJSON();

        // Steward decrypts connection request
        ConnectionRequest connectionRequest = stewardCodec.decryptMessage(MessageEnvelope.parseFromString(connectionRequestString, CONNECTION_REQUEST)).get();

        // Steward accepts connection request
        ConnectionResponse newcomerConnectionResponse = steward.acceptConnectionRequest(connectionRequest).get();

        // Steward sends a connection response
        String newcomerConnectionResponseString =  stewardCodec.encryptMessage(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, connectionRequest.getDid()).get().toJSON();


        MessageEnvelope<ConnectionResponse> connectionResponseEnvelope = MessageEnvelope.parseFromString(newcomerConnectionResponseString, CONNECTION_RESPONSE);
        // Newcomer decrypts the connection response
        ConnectionResponse connectionResponse = newcomerCodec.decryptMessage(connectionResponseEnvelope).get();

        // Newcomer accepts connection response
        newcomer.acceptConnectionResponse(connectionResponse, connectionResponseEnvelope.getDid()).get();

        // Faber needs a new DID to interact with identity owners, thus create a new DID request steward to write on ledger
        String verinymRequest = newcomerCodec.encryptMessage(newcomer.createVerinymRequest(connectionResponse.getDid()), IndyMessageTypes.VERINYM, connectionResponse.getDid()).get().toJSON();

        // #step 4.2.5 t/m 4.2.8
        // Steward accepts verinym request from Faber and thus writes the new DID on the ledger
        steward.acceptVerinymRequest(stewardCodec.decryptMessage(MessageEnvelope.parseFromString(verinymRequest, VERINYM)).get()).get();
    }

    static String onboardWalletOwner(TrustAnchor trustAnchor, IndyWallet newcomer) throws IndyException, ExecutionException, InterruptedException, IOException {
        // Create Codecs to facilitate encryption/decryption
        MessageEnvelopeCodec trustAnchorCodec = new MessageEnvelopeCodec(trustAnchor);
        MessageEnvelopeCodec newcomerCodec = new MessageEnvelopeCodec(newcomer);

        ConnectionRequest newcomerConnectionRequest = newcomer.createConnectionRequest().get();
        // Newcomer creates connection request for trust anchor
        String newcomerConnectionRequestString = newcomerCodec.encryptMessage(newcomerConnectionRequest, IndyMessageTypes.CONNECTION_REQUEST, trustAnchor.getMainDid()).get().toJSON();
        // Newcomer sends connectionRequest to trustAnchor
        MessageEnvelope<ConnectionRequest> connectionRequestMessageEnvelope = MessageEnvelope.parseFromString(newcomerConnectionRequestString, IndyMessageTypes.CONNECTION_REQUEST);

        //TrustAnchor decrypts message from newcomer
        ConnectionRequest connectionRequest = trustAnchorCodec.decryptMessage(connectionRequestMessageEnvelope).get();

        // TrustAnchor accepts connection request
        ConnectionResponse newcomerConnectionResponse = trustAnchor.acceptConnectionRequest(connectionRequest).get();
        // TrustAnchor sends connection response
        String newcomerConnectionResponseString = trustAnchorCodec.encryptMessage(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, connectionRequest.getDid()).get().toJSON();


        MessageEnvelope<ConnectionResponse> connectionResponseMessageEnvelope = MessageEnvelope.parseFromString(newcomerConnectionResponseString, CONNECTION_RESPONSE);
        // Newcomer accepts connection response
        ConnectionResponse connectionResponse = newcomerCodec.decryptMessage(MessageEnvelope.parseFromString(newcomerConnectionResponseString, CONNECTION_RESPONSE)).get();
        // TrustAnchor accepts the connectionResponse from the newcomer
        newcomer.acceptConnectionResponse(connectionResponse, connectionResponseMessageEnvelope.getDid()).get();


        return newcomerConnectionRequest.getDid();
    }
}
