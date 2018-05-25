package nl.quintor.studybits.university.services;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class IssuerService {

    private final IndyPool indyPool;

    private final TrustAnchor steward;

    private final Map<String, Issuer> issuers = new TreeMap<>(String::compareToIgnoreCase);

    @Autowired
    public IssuerService(IndyPool indyPool, @Qualifier("stewardTrustAnchor") TrustAnchor steward) {
        this.indyPool = indyPool;
        this.steward = steward;
    }

    public Issuer ensureIssuer(String issuerName) {
        return issuers.computeIfAbsent(
                issuerName,
                name -> createIssuer(issuerName)
                .orElseGet(() -> getIssuer(issuerName))
        );
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
        // Connect issuer
        ConnectionRequest connectionRequest = steward
                .createConnectionRequest(issuer.getName(), "TRUST_ANCHOR")
                .get();
        AnoncryptedMessage newcomerConnectionResponse = issuer
                .acceptConnectionRequest(connectionRequest)
                .thenCompose(AsyncUtil.wrapException(issuer::anonEncrypt))
                .get();
        steward.anonDecrypt(newcomerConnectionResponse, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptConnectionResponse))
                .get();
        AuthcryptedMessage verinym = issuer
                .authEncrypt(issuer.createVerinymRequest(connectionRequest.getDid()))
                .get();
        steward.authDecrypt(verinym, Verinym.class)
                .thenCompose(AsyncUtil.wrapException(steward::acceptVerinymRequest))
                .get();

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
        } catch (Exception ex) {
            if (ex.getCause() instanceof WalletExistsException) {
                return null;
            }
            throw ex;
        }

    }

    private String createSeed(String s) {
        return StringUtils.leftPad(s, 32, '0');
    }
}