package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor( onConstructor = @__( @Autowired ) )
public class IndyWalletService {
    private IndyPool indyPool;

    public IndyWallet create(String username) throws Exception {
        String seed = createSeed(username);

        return IndyWallet.create(indyPool, username, seed);
    }

    private String createSeed(String username) {
        return StringUtils.leftPad(username, 32, '0');
    }
}
