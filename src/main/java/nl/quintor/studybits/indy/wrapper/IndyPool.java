package nl.quintor.studybits.indy.wrapper;

import org.hyperledger.indy.sdk.pool.Pool;

public class IndyPool implements AutoCloseable {
    private Pool pool;
    private String poolName;
    private String handle;
    private String genesisTxnPath;

    public IndyPool open() {
        return null;
    }

    public String getPoolName() {
        return poolName;
    }

    @Override
    public void close() throws Exception {
        this.pool.closePoolLedger().get();
    }
}
