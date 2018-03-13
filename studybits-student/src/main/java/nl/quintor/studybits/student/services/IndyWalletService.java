package nl.quintor.studybits.student.services;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndyWalletService {
    @Autowired
    private IndyPoolService indyPoolService;

    public IndyWallet create(String username) throws Exception {
        IndyPool indyPool = indyPoolService.create();
        String seed = createSeed(username);

        return IndyWallet.create(indyPool, username, seed);
    }

    private String createSeed(String username) {
        return StringUtils.leftPad(username, 32, '0');
    }
}
