package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().exec("rm -rf /home/potte/.indy_client");

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

        Prover alice = new Prover("Alice", indyPool, IndyWallet.create(indyPool, "alice_wallet", null));
        String aliceFaberDid = onboardWalletOwner(faber, alice);
        alice.init("alice_master_secret");

        // Create schemas
        SchemaKey jobCertificateSchemaKey = government.createAndSendSchema("Job-Certificate", "0.2",
                "first_name", "last_name", "salary", "employee_status", "experience").get();

        SchemaKey transcriptSchemaKey = government.createAndSendSchema("Transcript", "1.2",
                "first_name", "last_name", "degree", "status", "year", "average", "ssn").get();


        // Create claim definitions
        faber.defineClaim(transcriptSchemaKey).get();

        acme.defineClaim(jobCertificateSchemaKey).get();



        AuthcryptedMessage transcriptClaimOffer = faber.createClaimOffer(transcriptSchemaKey, aliceFaberDid)
                                                    .thenCompose(AsyncUtil.wrapException(faber::authcrypt)).get();


        AuthcryptedMessage transcriptClaimRequest = alice.authDecrypt(transcriptClaimOffer, ClaimOffer.class)
        .thenCompose(AsyncUtil.wrapException(alice::storeClaimOfferAndCreateClaimRequest))
                .thenCompose(AsyncUtil.wrapException(alice::authcrypt)).get();


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
                .thenCompose(AsyncUtil.wrapException(faber::authcrypt)).get();


        alice.authDecrypt(claim, Claim.class)
                .thenCompose(AsyncUtil.wrapException(alice::storeClaim)).get();
    }

    private static void onboardIssuer(TrustAnchor steward, Issuer newcomer) throws InterruptedException, java.util.concurrent.ExecutionException, IndyException, java.io.IOException {
        // Connecting newcomer with Steward
        String governmentConnectionRequest = steward.createConnectionRequest(newcomer.getName(), "TRUST_ANCHOR").get().toJSON();

        AnoncryptedMessage newcomerConnectionResponse = newcomer.acceptConnectionRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class))
                .thenCompose(AsyncUtil.wrapException(newcomer::anoncrypt))
                .get();

        steward.anonDecrypt(newcomerConnectionResponse, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptConnectionResponse)).get();

        AuthcryptedMessage verinym = newcomer.createVerinymRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class).getDid())
                .thenCompose(AsyncUtil.wrapException(newcomer::authcrypt)).get();

        steward.authDecrypt(verinym, Verinym.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptVerinymRequest)).get();

        newcomer.init();
    }

    private static String onboardWalletOwner(TrustAnchor trustAnchor, WalletOwner newcomer) throws IndyException, ExecutionException, InterruptedException, IOException {
        String governmentConnectionRequest = trustAnchor.createConnectionRequest(newcomer.getName(), null).get().toJSON();

        AnoncryptedMessage newcomerConnectionResponse = newcomer.acceptConnectionRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class))
                .thenCompose(AsyncUtil.wrapException(newcomer::anoncrypt)).get();

        String newcomerDid = trustAnchor.anonDecrypt(newcomerConnectionResponse, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(trustAnchor::acceptConnectionResponse)).get();

        return newcomerDid;
    }
}
