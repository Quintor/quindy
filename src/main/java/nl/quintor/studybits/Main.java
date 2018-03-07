package nl.quintor.studybits;

import nl.quintor.studybits.indy.wrapper.*;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;

public class Main {
    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().exec("rm -rf /home/potte/.indy_client");

        String poolName = PoolUtils.createPoolLedgerConfig();

        IndyPool indyPool = new IndyPool(poolName);
        TrustAnchor steward = new TrustAnchor("Steward", indyPool, IndyWallet.create(indyPool, "steward_wallet", "000000000000000000000000Steward1"));


        Issuer government = Issuer.create("Government", indyPool, IndyWallet.create(indyPool, "government_wallet",null));
        onboardTrustAnchor(steward, government);

        Issuer faber = Issuer.create("Faber", indyPool, IndyWallet.create(indyPool, "faber_wallet", null));
        onboardTrustAnchor(steward, faber);

        Issuer acme = Issuer.create("Acme", indyPool, IndyWallet.create(indyPool, "acme_wallet", null));
        onboardTrustAnchor(steward, acme);

        Issuer thrift = Issuer.create("Thrift", indyPool, IndyWallet.create(indyPool, "thrift_wallet", null));
        onboardTrustAnchor(steward, thrift);

        SchemaKey jobCertificateSchemaKey = government.createAndSendSchema("Job-Certificate", "0.2",
                "first_name", "last_name", "salary", "employee_status", "experience").get();

        SchemaKey transcriptSchemaKey = government.createAndSendSchema("Transcript", "1.2",
                "first_name", "last_name", "degree", "status", "year", "average", "ssn").get();

    }

    private static void onboardTrustAnchor(TrustAnchor steward, TrustAnchor newcomer) throws InterruptedException, java.util.concurrent.ExecutionException, IndyException, java.io.IOException {
        // Connecting newcomer with Steward
        String governmentConnectionRequest = steward.createConnectionRequest(newcomer.getName(), "TRUST_ANCHOR").get().toJSON();

        EncryptedMessage governmentConnectionResponse = newcomer.acceptConnectionRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class)).get();

        steward.acceptConnectionResponse(governmentConnectionResponse).get();

        EncryptedMessage verinym = newcomer.createVerinymRequest(JSONUtil.mapper.readValue(governmentConnectionRequest, ConnectionRequest.class).getDid()).get();

        steward.acceptVerinymRequest(verinym).get();
    }
}
