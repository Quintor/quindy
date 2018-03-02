package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pairwise.Pairwise;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class WalletOwner {
    IndyPool pool;
    IndyWallet wallet;
    String name;

    WalletOwner(String name, IndyPool pool, IndyWallet wallet) {
        this.name = name;
        this.pool = pool;
        this.wallet = wallet;
    }

    CompletableFuture<String> signAndSubmitRequest(String request) throws IndyException {
        return Ledger.signAndSubmitRequest(pool.getPool(), wallet.getWallet(), wallet.getMainDid(), request);
    }

    public CompletableFuture<EncryptedMessage> acceptConnectionRequest(ConnectionRequest connectionRequest) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        log.debug("Calling keyForDid with {}, {}, {}", pool.getPool(), wallet.getWallet(), connectionRequest.getDid());
        return anoncrypt(wallet.newDid()
                .thenCombineAsync(Did.keyForDid(pool.getPool(), wallet.getWallet(), connectionRequest.getDid()),
                        (myDid, theirKey) -> new ConnectionResponse(myDid.getDid(), myDid.getVerkey(), connectionRequest.getNonce(), theirKey))
                .thenCompose(wrapException((ConnectionResponse connectionResponse) ->
                        storeDidAndPairwise(connectionResponse.getDid(), connectionRequest.getDid(), connectionResponse.getVerkey(), connectionResponse.getTheirKey())
                        .thenApply((_void) -> connectionResponse))));
    }

    CompletableFuture<Void> storeDidAndPairwise(String myDid, String theirDid, String myKey, String theirKey) throws JsonProcessingException, IndyException {
        log.debug("Storing theirDid: {}", theirDid);
        return Did.storeTheirDid(wallet.getWallet(), new TheirDidInfo(theirDid).toJSON())
                .thenCompose(wrapException(
                        (storeDidResponse) -> {
                            log.debug("Creating pairwise theirDid: {}, myDid: {}, metadata: {}", theirDid, myDid, new PairwiseMetadata(myKey, theirKey).toJSON());
                            return Pairwise.createPairwise(wallet.getWallet(), theirDid, myDid,
                                    new PairwiseMetadata(myKey, theirKey).toJSON());
                        }));
    }

    private CompletableFuture<EncryptedMessage> anoncrypt(CompletableFuture<? extends AnonCryptable> messageFuture) throws JsonProcessingException, IndyException {
        return messageFuture.thenCompose(wrapException(
                (AnonCryptable message) -> {
                    log.debug("Anoncrypting message: {}", message.toJSON());
                    return Crypto.anonCrypt(message.getTheirKey(), message.toJSON().getBytes(Charset.forName("utf8")))
                            .thenApply(cryptedMessage -> new EncryptedMessage(cryptedMessage, message.getTheirKey()));
                }
        ));
    }

    <T extends AnonCryptable> CompletableFuture<T> anonDecrypt(EncryptedMessage message, Class<T> valueType) throws IndyException {
        return Crypto.anonDecrypt(wallet.getWallet(), message.getVerkey(), message.getMessage())
                .thenApply(wrapException((decryptedMessage) -> JSONUtil.mapper.readValue(new String(decryptedMessage, Charset.forName("utf8")), valueType)));
    }

    CompletableFuture<EncryptedMessage> authcrypt(CompletableFuture<? extends AuthCryptable> messageFuture) throws JsonProcessingException, IndyException {
        return messageFuture.thenCompose(wrapException(
                (message) -> {
                    log.debug("Authcrypting message: {}", message.toJSON());
                    return Crypto.authCrypt(wallet.getWallet(),message.getMyKey(), message.getTheirKey(), message.toJSON().getBytes(Charset.forName("utf8")))
                            .thenApply(cryptedMessage -> new EncryptedMessage(cryptedMessage, message.getTheirKey()));
                }
        ));
    }

    <T extends AuthCryptable> CompletableFuture<T> authDecrypt(EncryptedMessage message, Class<T> valueType) throws IndyException {
        return Crypto.authDecrypt(wallet.getWallet(), message.getVerkey(), message.getMessage())
                .thenApply(wrapException((decryptedMessage) -> {
                    T decryptedObject = JSONUtil.mapper.readValue(new String(decryptedMessage.getDecryptedMessage(), Charset.forName("utf8")), valueType);
                    decryptedObject.setTheirKey(decryptedMessage.getVerkey());
                    return decryptedObject;
                }));
    }
}
