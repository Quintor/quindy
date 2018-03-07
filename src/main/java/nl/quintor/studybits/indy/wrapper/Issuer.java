package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.DidResults;

import java.util.concurrent.ExecutionException;

public class Issuer extends TrustAnchor {
    @Getter
    private String issuerDid;
    @Getter
    private String issuerKey;

    public Issuer(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }

    public static Issuer create(String name, IndyPool pool, IndyWallet wallet) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        Issuer issuer = new Issuer(name, pool, wallet);
        DidResults.CreateAndStoreMyDidResult issuerDidAndKey = wallet.newDid().get();
        issuer.issuerDid = issuerDidAndKey.getDid();
        issuer.issuerKey = issuerDidAndKey.getVerkey();

        return issuer;
    }
}
