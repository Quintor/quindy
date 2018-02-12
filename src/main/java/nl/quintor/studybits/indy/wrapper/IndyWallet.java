package nl.quintor.studybits.indy.wrapper;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.concurrent.ExecutionException;

public class IndyWallet implements AutoCloseable {
    private Wallet wallet;
    private String name;
    private String did;
    private String verKey;

    public IndyWallet( String name ) throws IndyException, ExecutionException, InterruptedException {
        this.wallet = Wallet.openWallet(name, null, null).get();
        this.name = name;
    }

    public static IndyWallet create( IndyPool pool, String name, String seed ) throws IndyException, ExecutionException, InterruptedException {
        Wallet.createWallet(pool.getPoolName(), name, "default", null, null).get();

        IndyWallet indyWallet = new IndyWallet(name);
        String seedJSON = String.format("{'seed': '%s'}",seed);
        DidResults.CreateAndStoreMyDidResult result = Did.createAndStoreMyDid(indyWallet.wallet, seedJSON).get();

        indyWallet.did = result.getDid();
        indyWallet.verKey = result.getVerkey();

        return indyWallet;
    }

    @Override
    public void close() throws Exception {
        wallet.closeWallet();
    }

    public static void delete( String name ) throws IndyException {
        Wallet.deleteWallet(name, null);
    }


}
