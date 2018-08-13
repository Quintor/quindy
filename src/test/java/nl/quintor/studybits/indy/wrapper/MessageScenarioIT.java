package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.TestUtil.removeIndyClientDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class MessageScenarioIT {

    @Test
    public void fullScenarioTest() throws Exception {
        removeIndyClientDirectory();
        Pool.setProtocolVersion(PoolUtils.PROTOCOL_VERSION).get();

        String poolName = PoolUtils.createPoolLedgerConfig(null);
        IndyPool indyPool = new IndyPool(poolName);
        TrustAnchor steward = new TrustAnchor(IndyWallet.create(indyPool, "steward_wallet", "000000000000000000000000Steward1"));

        // Onboard the issuers (onboard -> verinym -> issuerDids)
        Issuer government = new Issuer(IndyWallet.create(indyPool, "government_wallet",null));
        onboardIssuer(steward, government);

        Issuer faber = new Issuer(IndyWallet.create(indyPool, "faber_wallet", null));
        onboardIssuer(steward, faber);


        IndyWallet acmeWallet = IndyWallet.create(indyPool, "acme_wallet", null);
        Issuer acme = new Issuer(acmeWallet);
        onboardIssuer(steward, acme);

        Issuer thrift = new Issuer(IndyWallet.create(indyPool, "thrift_wallet", null));
        onboardIssuer(steward, thrift);

        Prover alice = new Prover(IndyWallet.create(indyPool, "alice_wallet", null), "alice_master_secret");
        String aliceFaberDid = onboardWalletOwner(faber, alice);
        alice.init();

        String aliceAcmeDid = onboardWalletOwner(acme, alice);

        // Create schemas
        String jobCertificateSchemaId = government.createAndSendSchema("Job-Certificate", "0.2",
                "first_name", "last_name", "salary", "employee_status", "experience").get();

        String transcriptSchemaId = government.createAndSendSchema("Transcript", "1.2",
                "first_name", "last_name", "degree", "status", "year", "average", "ssn").get();


        // Create credential definitions
        String transcriptCredentialDefId = faber.defineCredential(transcriptSchemaId).get();

        String jobCertificateCredentialDefId = acme.defineCredential(jobCertificateSchemaId).get();



        CredentialOffer transcriptCredentialOffer = faber.createCredentialOffer(transcriptCredentialDefId, aliceFaberDid).get();
        String credentialOfferMessageEnvelope = MessageEnvelope.fromAuthcryptable(transcriptCredentialOffer, IndyMessageTypes.CREDENTIAL_OFFER, faber).toJSON();


        CredentialOffer decryptedTranscriptCredentialOffer = MessageEnvelope.<CredentialOffer>parseFromString(credentialOfferMessageEnvelope, alice).getMessage();
        CredentialRequest transcriptCredentialRequest = alice.createCredentialRequest(decryptedTranscriptCredentialOffer).get();
        String transcriptCredentialRequestMessageEnvelope = MessageEnvelope.<CredentialRequest>fromAuthcryptable(transcriptCredentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST, alice).toJSON();


        Map<String, Object> credentialValues  = new HashMap<>();
        credentialValues.put("first_name", "Alice");
        credentialValues.put("last_name", "Garcia");
        credentialValues.put("degree", "Bachelor of Science, Marketing");
        credentialValues.put("status", "graduated");
        credentialValues.put("ssn", "123-45-6789");
        credentialValues.put("year", 2015);
        credentialValues.put("average", 5);

        CredentialRequest credentialRequest = MessageEnvelope.<CredentialRequest>parseFromString(transcriptCredentialRequestMessageEnvelope, faber).getMessage();
        CredentialWithRequest credential = faber.createCredential(credentialRequest, credentialValues).get();
        String credentialMessageEnvelope = MessageEnvelope.fromAuthcryptable(credential, IndyMessageTypes.CREDENTIAL, faber).toJSON();


        CredentialWithRequest decryptedCredential = MessageEnvelope.<CredentialWithRequest>parseFromString(credentialMessageEnvelope, alice).getMessage();
        alice.storeCredential(decryptedCredential).get();

        List<CredentialInfo> credentialInfos = alice.findAllCredentials()
                .get();

        System.out.println(credentialInfos);


        List<Filter> transcriptFilter = Collections.singletonList(new Filter(transcriptCredentialDefId));
        ProofRequest jobApplicationProofRequest = ProofRequest.builder()
                .name("Job-Application")
                .nonce("1432422343242122312411212")
                .version("0.1")
                .requestedAttribute("attr1_referent", new AttributeInfo("first_name", Optional.empty()))
                .requestedAttribute("attr2_referent", new AttributeInfo("last_name", Optional.empty()))
                .requestedAttribute("attr3_referent", new AttributeInfo("degree", Optional.of(transcriptFilter)))
                .requestedAttribute("attr4_referent", new AttributeInfo("status", Optional.of(transcriptFilter)))
                .requestedAttribute("attr5_referent", new AttributeInfo("ssn", Optional.of(transcriptFilter)))
                .requestedAttribute("attr6_referent", new AttributeInfo("phone_number", Optional.empty()))
                .requestedPredicate("predicate1_referent", new PredicateInfo("average", ">=", 4, Optional.of(transcriptFilter)))
                .build();

        jobApplicationProofRequest.setTheirDid(aliceAcmeDid);

        String jobApplicationProofRequestEnvelope = MessageEnvelope.fromAuthcryptable(jobApplicationProofRequest, IndyMessageTypes.PROOF_REQUEST, acme).toJSON();


        Map<String, String> selfAttestedAttributes = new HashMap<>();
        selfAttestedAttributes.put("first_name", "Alice");
        selfAttestedAttributes.put("last_name", "Garcia");
        selfAttestedAttributes.put("phone_number", "123phonenumber");

        ProofRequest decryptedProofRequest = MessageEnvelope.<ProofRequest>parseFromString(jobApplicationProofRequestEnvelope, alice).getMessage();

        Proof proof = alice.fulfillProofRequest(decryptedProofRequest, selfAttestedAttributes).get();
        String proofEnvelope = MessageEnvelope.fromAuthcryptable(proof, IndyMessageTypes.PROOF, alice).toJSON();

        Proof decryptedProof = MessageEnvelope.<Proof>parseFromString(proofEnvelope, acme).getMessage();

        List<ProofAttribute> attributes = new Verifier(acmeWallet).getVerifiedProofAttributes(jobApplicationProofRequest, decryptedProof).get();

        System.out.println(attributes);
        assertThat(attributes, containsInAnyOrder(
                new ProofAttribute("attr1_referent", "first_name", "Alice"),
                new ProofAttribute("attr2_referent", "last_name", "Garcia"),
                new ProofAttribute("attr3_referent", "degree", "Bachelor of Science, Marketing"),
                new ProofAttribute("attr4_referent", "status", "graduated"),
                new ProofAttribute("attr5_referent", "ssn", "123-45-6789"),
                new ProofAttribute("attr6_referent", "phone_number", "123phonenumber")
                ));
    }

    public static void onboardIssuer(TrustAnchor steward, Issuer newcomer) throws InterruptedException, ExecutionException, IndyException, IOException {
        // Connecting newcomer with Steward
        String governmentConnectionRequest = steward.createConnectionRequest(newcomer.getName(), "TRUST_ANCHOR")
                .thenApply(connectionRequest -> new MessageEnvelope<>(IndyMessageTypes.CONNECTION_REQUEST, connectionRequest, null, steward, null)).get().toJSON();

        MessageEnvelope<ConnectionRequest> connectionRequestMessageEnvelope = MessageEnvelope.parseFromString(governmentConnectionRequest, newcomer);
        ConnectionResponse newcomerConnectionResponse = newcomer.acceptConnectionRequest(connectionRequestMessageEnvelope.getMessage()).get();
        String newcomerConnectionResponseString =  MessageEnvelope.fromAnoncryptable(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, newcomer).toJSON();
        ConnectionResponse connectionResponse = MessageEnvelope.<ConnectionResponse>parseFromString(newcomerConnectionResponseString, steward).getMessage();
        steward.acceptConnectionResponse(connectionResponse).get();

        String verinymRequest = MessageEnvelope.fromAuthcryptable(newcomer.createVerinymRequest(MessageEnvelope.<ConnectionRequest>parseFromString(governmentConnectionRequest, newcomer).getMessage()
                .getDid()), IndyMessageTypes.VERINYM, newcomer).toJSON();


        steward.acceptVerinymRequest(MessageEnvelope.<Verinym>parseFromString(verinymRequest, steward).getMessage()).get();
    }

    private static String onboardWalletOwner(TrustAnchor trustAnchor, IndyWallet newcomer) throws IndyException, ExecutionException, InterruptedException, IOException {
        String governmentConnectionRequest = trustAnchor.createConnectionRequest(newcomer.getName(), null)
                .thenApply(connectionRequest -> new MessageEnvelope<>(IndyMessageTypes.CONNECTION_REQUEST, connectionRequest, null, trustAnchor, null)).get().toJSON();


        MessageEnvelope<ConnectionRequest> connectionRequestMessageEnvelope = MessageEnvelope.parseFromString(governmentConnectionRequest, newcomer);
        ConnectionResponse newcomerConnectionResponse = newcomer.acceptConnectionRequest(connectionRequestMessageEnvelope.getMessage()).get();
        String newcomerConnectionResponseString =  MessageEnvelope.fromAnoncryptable(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, newcomer).toJSON();

        ConnectionResponse connectionResponse = MessageEnvelope.<ConnectionResponse>parseFromString(newcomerConnectionResponseString, trustAnchor).getMessage();
        String newcomerDid = trustAnchor.acceptConnectionResponse(connectionResponse).get();

        return newcomerDid;
    }
}
