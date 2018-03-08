package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.MyDidInfo;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class IndyWallet implements AutoCloseable {
    @Getter
    private Wallet wallet;
    private String name;
    @Getter
    private String mainDid;
    @Getter
    private String mainKey;

    public IndyWallet( String name ) throws IndyException, ExecutionException, InterruptedException {
        this.wallet = Wallet.openWallet(name, null, null).get();
        this.name = name;
    }

    public static IndyWallet create( IndyPool pool, String name, String seed ) throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        Wallet.createWallet(pool.getPoolName(), name, "default", null, null).get();

        IndyWallet indyWallet = new IndyWallet(name);

        DidResults.CreateAndStoreMyDidResult result = indyWallet.newDid(seed).get();
        indyWallet.mainDid = result.getDid();
        indyWallet.mainKey = result.getVerkey();

        return indyWallet;
    }

    CompletableFuture<DidResults.CreateAndStoreMyDidResult> newDid() throws JsonProcessingException, IndyException {
        return newDid(null);
    }

    CompletableFuture<DidResults.CreateAndStoreMyDidResult> newDid(String seed) throws JsonProcessingException, IndyException {
        String seedJSON = StringUtils.isNotBlank(seed)  ? (new MyDidInfo(seed)).toJSON() : "{}";
        log.debug("Creating new did with seedJSON: {}", seedJSON);
        return Did.createAndStoreMyDid(wallet, seedJSON);
    }

    @Override
    public void close() throws Exception {
        wallet.closeWallet();
    }

    public static void delete( String name ) throws IndyException {
        Wallet.deleteWallet(name, null);
    }


}
