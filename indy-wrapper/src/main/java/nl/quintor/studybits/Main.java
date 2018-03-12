package nl.quintor.studybits;

import nl.quintor.studybits.indy.wrapper.*;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;

import java.io.IOException;
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


        // Create schemas
        SchemaKey jobCertificateSchemaKey = government.createAndSendSchema("Job-Certificate", "0.2",
                "first_name", "last_name", "salary", "employee_status", "experience").get();

        SchemaKey transcriptSchemaKey = government.createAndSendSchema("Transcript", "1.2",
                "first_name", "last_name", "degree", "status", "year", "average", "ssn").get();


        // Create claim definitions
        faber.defineClaim(transcriptSchemaKey).get();

        acme.defineClaim(jobCertificateSchemaKey).get();

        Prover alice = new Prover("Alice", indyPool, IndyWallet.create(indyPool, "alice_wallet", null));
        String aliceFaberDid = onboardWalletOwner(faber, alice);

        AuthcryptedMessage transcriptClaimOffer = faber.createClaimOffer(transcriptSchemaKey, aliceFaberDid)
                                                    .thenCompose(AsyncUtil.wrapException(faber::authcrypt)).get();


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
