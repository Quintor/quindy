package nl.quintor.studybits.indy.wrapper;

import lombok.Getter;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;

import java.util.concurrent.ExecutionException;

@Getter
public class IndyPool implements AutoCloseable {
    private Pool pool;
    private String poolName;

    public IndyPool(String poolName) throws IndyException, ExecutionException, InterruptedException {
        this.poolName = poolName;
        this.pool = Pool.openPoolLedger(poolName, "{}").get();
    }

    @Override
    public void close() throws Exception {
        this.pool.closePoolLedger().get();
    }
}

