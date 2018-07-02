package nl.quintor.studybits.indy.wrapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class IndyLedger implements LookupRepository {
    private Pool pool;

    public IndyLedger(Pool pool) {
        this.pool = pool;
    }
    public CompletableFuture<String> getKeyForDid(String did, Wallet wallet) throws IndyException {
        log.debug("Called getKeyForDid: {}", did);
        return Did.keyForDid(pool, wallet, did)
                .thenApply(key -> {
                    log.debug("Got key for did {} key {}", did, key);
                    return key;
                });
    }

    @Override
    public CompletableFuture<String> submitRequest(String request) throws IndyException {
        return Ledger.submitRequest(pool,request);
    }

    @Override
    public CompletableFuture<String> signAndSubmitRequest(String request, String did, Wallet wallet) throws IndyException {
        return Ledger.signAndSubmitRequest(pool, wallet, did, request);
    }
}
