package nl.quintor.studybits;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.TrustAnchor;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class RugBeanConfig {

    @Autowired
    IndyPool indyPool;

    @Value("${university.walletname}")
    private String universityWalletName;

    @Value("${university.walletseed}")
    private String universityWalletSeed;

    @Value("${university.steward.url}")
    private String universityStewardUrl;

    @Bean("universityTrustAnchor")
    public TrustAnchor universityTrustAnchor() throws Exception {
        IndyWallet universityWallet = getIndyWallet(universityWalletName, universityWalletSeed);
        TrustAnchor university = new TrustAnchor("University", indyPool, universityWallet);
        return university;
    }

    private IndyWallet getIndyWallet(String name, String seed) throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        try {
            return IndyWallet.create(indyPool, name, seed);
        }catch (Exception ex) {
            if(!(ex.getCause() instanceof WalletExistsException)) {
                throw ex;
            }
        }
        return new IndyWallet(name);

    }

}