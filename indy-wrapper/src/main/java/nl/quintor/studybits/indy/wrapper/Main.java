package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.apache.commons.io.FileUtils;
import org.hyperledger.indy.sdk.IndyException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws Exception {
        removeIndyClientDirectory();

        String poolName = PoolUtils.createPoolLedgerConfig();
        IndyPool indyPool = new IndyPool(poolName);
        TrustAnchor steward = new TrustAnchor("Steward", indyPool, IndyWallet.create(indyPool, "steward_wallet", "000000000000000000000000Steward1"));

        // Onboard the issuers (onboard -> verinym -> issuerDids)
        Issuer government = new Issuer("Government", indyPool, IndyWallet.create(indyPool, "government_wallet",null));
        onboardIssuer(steward, government);

        Issuer faber = new Issuer("Faber", indyPool, IndyWallet.create(indyPool, "faber_wallet", null));
        onboardIssuer(steward, faber);

        Issuer acme = new Issuer("Acme", indyPool, IndyWallet.create(indyPool, "acme_wallet", null));
        onboardIssuer(steward, acme);

        Issuer thrift = new Issuer("Thrift", indyPool, IndyWallet.create(indyPool, "thrift_wallet", null));
        onboardIssuer(steward, thrift);

        Prover alice = new Prover("Alice", indyPool, IndyWallet.create(indyPool, "alice_wallet", null), "alice_master_secret");
        String aliceFaberDid = onboardWalletOwner(faber, alice);
        alice.init();

        String aliceAcmeDid = onboardWalletOwner(acme, alice);

        // Create schemas
        SchemaKey jobCertificateSchemaKey = government.createAndSendSchema("Job-Certificate", "0.2",
                "first_name", "last_name", "salary", "employee_status", "experience").get();

        SchemaKey transcriptSchemaKey = government.createAndSendSchema("Transcript", "1.2",
                "first_name", "last_name", "degree", "status", "year", "average", "ssn").get();


        // Create claimModel definitions
        faber.defineClaim(transcriptSchemaKey).get();

        acme.defineClaim(jobCertificateSchemaKey).get();



        AuthcryptedMessage transcriptClaimOffer = faber.createClaimOffer(transcriptSchemaKey, aliceFaberDid)
                .thenCompose(AsyncUtil.wrapException(faber::authEncrypt)).get();


        AuthcryptedMessage transcriptClaimRequest = alice.authDecrypt(transcriptClaimOffer, ClaimOffer.class)
        .thenCompose(AsyncUtil.wrapException(alice::storeClaimOfferAndCreateClaimRequest))
                .thenCompose(AsyncUtil.wrapException(alice::authEncrypt)).get();


        Map<String, Object> claimValues  = new HashMap<>();
        claimValues.put("first_name", "Alice");
        claimValues.put("last_name", "Garcia");
        claimValues.put("degree", "Bachelor of Science, Marketing");
        claimValues.put("status", "graduated");
        claimValues.put("ssn", "123-45-6789");
        claimValues.put("year", 2015);
        claimValues.put("average", 5);

        AuthcryptedMessage claim = faber.authDecrypt(transcriptClaimRequest, ClaimRequest.class)
                .thenCompose(AsyncUtil.wrapException(claimRequest -> faber.createClaim(claimRequest, claimValues)))
                .thenCompose(AsyncUtil.wrapException(faber::authEncrypt)).get();


        alice.authDecrypt(claim, Claim.class)
                .thenCompose(AsyncUtil.wrapException(alice::storeClaim)).get();

        List<ClaimInfo> claims = alice.findAllClaims()
                                      .get();

        System.out.println(claims);


        List<Filter> transcriptFilter = Collections.singletonList(new Filter(faber.getIssuerDid(), transcriptSchemaKey));
        ProofRequest jobApplicationProofRequest = ProofRequest.builder()
                                                              .name("Job-Application")
                                                              .nonce("1432422343242122312411212")
                                                              .version("0.1")
                                                              .requestedAttr("attr1_referent", new AttributeInfo("first_name", Optional.empty()))
                                                              .requestedAttr("attr2_referent", new AttributeInfo("last_name", Optional.empty()))
                                                              .requestedAttr("attr3_referent", new AttributeInfo("degree", Optional.of(transcriptFilter)))
                                                              .requestedAttr("attr4_referent", new AttributeInfo("status", Optional.of(transcriptFilter)))
                                                              .requestedAttr("attr5_referent", new AttributeInfo("ssn", Optional.of(transcriptFilter)))
                                                              .requestedAttr("attr6_referent", new AttributeInfo("phone_number", Optional.empty()))
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
                .thenCompose(proof -> acme.getVerifiedProofAttributes(jobApplicationProofRequest, proof))
                .get();

        System.out.println(attributes);
    }

    private static void onboardIssuer(TrustAnchor steward, Issuer newcomer) throws InterruptedException, java.util.concurrent.ExecutionException, IndyException, java.io.IOException {
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

        newcomer.init();
    }

    private static String onboardWalletOwner(TrustAnchor trustAnchor, WalletOwner newcomer) throws IndyException, ExecutionException, InterruptedException, IOException {
        String governmentConnectionRequest = trustAnchor.createConnectionRequest(newcomer.getName(), null).get().toJSON();

        AnoncryptedMessage newcomerConnectionResponse = newcomer.acceptConnectionRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class))
                .thenCompose(AsyncUtil.wrapException(newcomer::anonEncrypt)).get();

        String newcomerDid = trustAnchor.anonDecrypt(newcomerConnectionResponse, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(trustAnchor::acceptConnectionResponse)).get();

        return newcomerDid;
    }

    private static void removeIndyClientDirectory() throws Exception {
        String homeDir = System.getProperty("user.home");
        File indyClientDir = Paths.get(homeDir, ".indy_client")
                                  .toFile();
        FileUtils.deleteDirectory(indyClientDir);
    }
}
