package nl.quintor.studybits.student.services;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.springframework.stereotype.Component;

@Component
public class IndyPoolService {
    public IndyPool create() throws Exception {
        String poolName = PoolUtils.createPoolLedgerConfig();
        return new IndyPool(poolName);
    }
}
