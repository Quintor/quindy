package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import nl.quintor.studybits.indy.wrapper.util.SeedUtil;
import org.hamcrest.CoreMatchers;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static nl.quintor.studybits.indy.wrapper.TestUtil.*;
import static nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes.CREDENTIAL_OFFER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;


public class IntegerProofIT {
    @Test
    public void IntegerProofTest() throws Exception {
        removeIndyClientDirectory();

        // #Step 2
        // Set pool protocol version based on PoolUtils
        Pool.setProtocolVersion(PoolUtils.PROTOCOL_VERSION).get();

        // Initialize message types for MessageEnvelope
        IndyMessageTypes.init();

        // Create and open Pool with 'default_pool' as pool name
        String poolName = PoolUtils.createPoolLedgerConfig(null, "testPool" + System.currentTimeMillis());
        IndyPool indyPool = new IndyPool(poolName);


        TrustAnchor steward = new TrustAnchor(IndyWallet.create(indyPool, "steward" + System.currentTimeMillis(), "000000000000000000000000Steward1"));

        steward.acceptVerinymRequest(new Verinym(steward.getMainDid(), steward.getMainDid()));

        Issuer stewardIssuer = new Issuer(steward);
        Verifier stewardVerifier = new Verifier(steward);
        MessageEnvelopeCodec stewardCodec = new MessageEnvelopeCodec(stewardIssuer);

        String schemaId = stewardIssuer.createAndSendSchema("AwesomeNumber", "0.1", "number").get();

        String credentialDefinitionId = stewardIssuer.defineCredential(schemaId).get();



        Prover alice = new Prover(IndyWallet.create(indyPool, "alice" + System.currentTimeMillis(), SeedUtil.generateSeed()), "master_secret");

        alice.init();

        String aliceStewardDid = onboardWalletOwner(stewardIssuer, alice);

        CredentialOffer transcriptCredentialOffer = stewardIssuer.createCredentialOffer(credentialDefinitionId, aliceStewardDid).get();
        MessageEnvelope<CredentialOffer> convertedEnvelope = MessageEnvelope.parseFromString(stewardCodec.encryptMessage(transcriptCredentialOffer, CREDENTIAL_OFFER, aliceStewardDid).get().toJSON(), CREDENTIAL_OFFER);

        CredentialRequest credentialRequest = alice.createCredentialRequest(convertedEnvelope.getDid(), transcriptCredentialOffer).get();

        Map<String, Object> credentialValues  = new HashMap<>();
        credentialValues.put("number", 5);


        CredentialWithRequest credential = stewardIssuer.createCredential(credentialRequest, credentialValues).get();

        alice.storeCredential(credential).get();


        List<Filter> filter = Collections.singletonList(new Filter(credentialDefinitionId));
        ProofRequest proofRequest = ProofRequest.builder()
                .name("FavouriteNumberPRoof")
                .nonce("123432421212")
                .version("0.1")
                .requestedAttribute("attr1_referent", new AttributeInfo("number", Optional.of(filter)))
                .build();

        Map<String, String> emptyAttributes = new HashMap<>();

        Proof proof = alice.fulfillProofRequest(proofRequest, emptyAttributes).get();

        List<ProofAttribute> proofAttributes = stewardVerifier.getVerifiedProofAttributes(proofRequest, proof).get();

        assertThat(proofAttributes, hasSize(1));
        assertThat(proofAttributes.get(0), equalTo(new ProofAttribute("attr1_referent","number","5")));
    }
}
