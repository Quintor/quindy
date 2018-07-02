package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.TestUtil.removeIndyClientDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScenarioIT {

    @Test
    public void fullScenarioTest() throws Exception {
        removeIndyClientDirectory();

        String poolName = PoolUtils.createPoolLedgerConfig(null);
        IndyPool indyPool = new IndyPool(poolName);
        LookupRepository lookupRepository = new IndyLedger(indyPool.getPool());
        TrustAnchor steward = new TrustAnchor(IndyWallet.create(lookupRepository, indyPool.getPoolName(), "steward_wallet", "000000000000000000000000Steward1"));

        // Onboard the issuers (onboard -> verinym -> issuerDids)
        Issuer government = new Issuer(IndyWallet.create(lookupRepository, indyPool.getPoolName(), "government_wallet",null));
        onboardIssuer(steward, government);

        Issuer faber = new Issuer(IndyWallet.create(lookupRepository, indyPool.getPoolName(), "faber_wallet", null));
        onboardIssuer(steward, faber);


        IndyWallet acmeWallet = IndyWallet.create(lookupRepository, indyPool.getPoolName(), "acme_wallet", null);
        Issuer acme = new Issuer(acmeWallet);
        onboardIssuer(steward, acme);

        Issuer thrift = new Issuer(IndyWallet.create(lookupRepository, indyPool.getPoolName(), "thrift_wallet", null));
        onboardIssuer(steward, thrift);

        Prover alice = new Prover(IndyWallet.create(lookupRepository, indyPool.getPoolName(), "alice_wallet", null), "alice_master_secret");
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



        AuthcryptedMessage transcriptCredentialOffer = faber.createCredentialOffer(transcriptCredentialDefId, aliceFaberDid)
                .thenCompose(AsyncUtil.wrapException(faber::authEncrypt)).get();


        AuthcryptedMessage transcriptCredentialRequest = alice.authDecrypt(transcriptCredentialOffer, CredentialOffer.class)
                .thenCompose(AsyncUtil.wrapException(alice::createCredentialRequest))
                .thenCompose(AsyncUtil.wrapException(alice::authEncrypt)).get();


        Map<String, Object> credentialValues  = new HashMap<>();
        credentialValues.put("first_name", "Alice");
        credentialValues.put("last_name", "Garcia");
        credentialValues.put("degree", "Bachelor of Science, Marketing");
        credentialValues.put("status", "graduated");
        credentialValues.put("ssn", "123-45-6789");
        credentialValues.put("year", 2015);
        credentialValues.put("average", 5);

        AuthcryptedMessage credential = faber.authDecrypt(transcriptCredentialRequest, CredentialRequest.class)
                .thenCompose(AsyncUtil.wrapException(credentialRequest -> faber.createCredential(credentialRequest, credentialValues)))
                .thenCompose(AsyncUtil.wrapException(faber::authEncrypt)).get();


        alice.authDecrypt(credential, CredentialWithRequest.class)
                .thenCompose(AsyncUtil.wrapException(alice::storeCredential)).get();

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

        AuthcryptedMessage authcryptedJobApplicationProofRequest = acme.authEncrypt(jobApplicationProofRequest)
                .get();


        Map<String, String> selfAttestedAttributes = new HashMap<>();
        selfAttestedAttributes.put("first_name", "Alice");
        selfAttestedAttributes.put("last_name", "Garcia");
        selfAttestedAttributes.put("phone_number", "123phonenumber");

        AuthcryptedMessage authcryptedProof = alice.authDecrypt(authcryptedJobApplicationProofRequest, ProofRequest.class)
                .thenCompose(AsyncUtil.wrapException(proofRequest -> alice.fulfillProofRequest(proofRequest, selfAttestedAttributes)))
                .thenCompose(AsyncUtil.wrapException(alice::authEncrypt))
                .get();

        List<ProofAttribute> attributes = acme
                .authDecrypt(authcryptedProof, Proof.class)
                .thenCompose(proof -> new Verifier(acmeWallet).getVerifiedProofAttributes(jobApplicationProofRequest, proof))
                .get();

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

    public static void onboardIssuer(TrustAnchor steward, Issuer newcomer) throws InterruptedException, java.util.concurrent.ExecutionException, IndyException, java.io.IOException {
        // Connecting newcomer with Steward
        String governmentConnectionRequest = steward.createConnectionRequest(newcomer.getName(), "TRUST_ANCHOR").get().toJSON();

        AnoncryptedMessage newcomerConnectionResponse = newcomer.acceptConnectionRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class))
                .thenCompose(AsyncUtil.wrapException(newcomer::anonEncrypt))
                .get();

        steward.anonDecrypt(newcomerConnectionResponse, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptConnectionResponse)).get();

        AuthcryptedMessage verinym = newcomer.authEncrypt(newcomer.createVerinymRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class)
                .getDid()))
                .get();

        steward.authDecrypt(verinym, Verinym.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptVerinymRequest)).get();
    }

    private static String onboardWalletOwner(TrustAnchor trustAnchor, IndyWallet newcomer) throws IndyException, ExecutionException, InterruptedException, IOException {
        String governmentConnectionRequest = trustAnchor.createConnectionRequest(newcomer.getName(), null).get().toJSON();

        AnoncryptedMessage newcomerConnectionResponse = newcomer.acceptConnectionRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class))
                .thenCompose(AsyncUtil.wrapException(newcomer::anonEncrypt)).get();

        String newcomerDid = trustAnchor.anonDecrypt(newcomerConnectionResponse, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(trustAnchor::acceptConnectionResponse)).get();

        return newcomerDid;
    }
}
