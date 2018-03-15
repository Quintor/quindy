package nl.quintor.studybits;

import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.TrustAnchor;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UniversitiesBeanConfig {

    @Autowired
    private IndyPool indyPool;

    @Autowired
    private TrustAnchor steward;

    @Bean("RugUniversity")
    public Issuer rugUniversity() {
        return ensureIssuer("Rug");
    }

    @Bean("GentUniversity")
    public Issuer gentUniversity() {
        return ensureIssuer("Gent");
    }

    private Issuer ensureIssuer(String universityName) {
        return createIssuer(universityName)
                .orElseGet(() -> getIssuer(universityName));
    }

    private Optional<Issuer> createIssuer(String name) {
        IndyWallet wallet = createIndyWallet(name);
        return wallet != null ? Optional.of(onboardIssuer(wallet)) : Optional.empty();
    }

    private Issuer getIssuer(String name) {
        IndyWallet wallet = getIndyWallet(name);
        return new Issuer(name, indyPool, wallet);
    }

    @SneakyThrows
    private Issuer onboardIssuer(IndyWallet wallet) {
        Issuer issuer = new Issuer(wallet.getName(), indyPool, wallet);
        // Connect issuer with university
        ConnectionRequest connectionRequest = steward
                .createConnectionRequest(issuer.getName(), "TRUST_ANCHOR")
                .get();
        AnoncryptedMessage newcomerConnectionResponse = issuer
                .acceptConnectionRequest(connectionRequest)
                .thenCompose(AsyncUtil.wrapException(issuer::anoncrypt))
                .get();
        steward.anonDecrypt(newcomerConnectionResponse, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptConnectionResponse))
                .get();
        AuthcryptedMessage verinym = issuer
                .createVerinymRequest(connectionRequest.getDid())
                .thenCompose(AsyncUtil.wrapException(issuer::authcrypt))
                .get();
        steward.authDecrypt(verinym, Verinym.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptVerinymRequest))
                .get();
        issuer.init();
        return issuer;
    }

    @SneakyThrows
    private IndyWallet getIndyWallet(String name) {
        return new IndyWallet(name);
    }

    @SneakyThrows
    private IndyWallet createIndyWallet(String name) {
        try {
            return IndyWallet.create(indyPool, name, createSeed(name));
        }catch (Exception ex) {
            if(ex.getCause() instanceof WalletExistsException) {
                return null;
            }
            throw ex;
        }

    }

    private String createSeed(String s) { return StringUtils.leftPad(s, 32, '0'); }
}