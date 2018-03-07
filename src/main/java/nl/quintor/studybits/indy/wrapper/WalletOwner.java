package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pairwise.Pairwise;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;
import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapPredicateException;

@Slf4j
public class WalletOwner {
    IndyPool pool;
    IndyWallet wallet;
    @Getter
    String name;

    public WalletOwner(String name, IndyPool pool, IndyWallet wallet) {
        this.name = name;
        this.pool = pool;
        this.wallet = wallet;
    }

    CompletableFuture<String> signAndSubmitRequest(String request) throws IndyException {
        return signAndSubmitRequest(request, wallet.getMainDid());
    }

    CompletableFuture<String> signAndSubmitRequest(String request, String did) throws IndyException {
        return Ledger.signAndSubmitRequest(pool.getPool(), wallet.getWallet(), did, request);
    }

    public CompletableFuture<EncryptedMessage> acceptConnectionRequest(ConnectionRequest connectionRequest) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        log.debug("{} Calling acceptConnectionRequest with {}, {}, {}", name, pool.getPool(), wallet.getWallet(), connectionRequest.getDid());

        return anoncrypt(wallet.newDid()
                .thenCombineAsync(getKeyForDid(connectionRequest.getDid()),
                                                (myDid, theirKey) -> {
                                                        log.debug("{} Got theirKey {} with did {}", name, theirKey, connectionRequest.getDid());
                                                        return new ConnectionResponse(myDid.getDid(), myDid.getVerkey(), connectionRequest.getNonce(), theirKey);
                                                    })

                .thenCompose(wrapException((ConnectionResponse connectionResponse) ->
                        storeDidAndPairwise(connectionResponse.getDid(), connectionRequest.getDid(), connectionResponse.getVerkey(), connectionResponse.getTheirKey())
                        .thenApply((_void) -> connectionResponse))));
    }

    CompletableFuture<Void> storeDidAndPairwise(String myDid, String theirDid, String myKey, String theirKey) throws JsonProcessingException, IndyException {
        log.debug("{} Called storeDidAndPairwise: myDid: {}, theirDid: {}, myKey: {}, theirKey: {}", name, myDid, theirDid, myKey, theirKey);
        return Did.storeTheirDid(wallet.getWallet(), new TheirDidInfo(theirDid).toJSON())
                .thenCompose(wrapException(
                        (storeDidResponse) -> {
                            log.debug("Creating pairwise theirDid: {}, myDid: {}, metadata: {}", theirDid, myDid, new PairwiseMetadata(myKey, theirKey).toJSON());
                            return Pairwise.createPairwise(wallet.getWallet(), theirDid, myDid,
                                    new PairwiseMetadata(myKey, theirKey).toJSON());
                        }));
    }

    CompletableFuture<ListPairwiseResult> findPairwiseByTheirKey(String theirKey) throws IndyException {
        log.debug("{}: Finding pairwise by key {}", name, theirKey);
        return Pairwise.listPairwise(wallet.getWallet())
                .thenApply(wrapException((allPairwise) -> {
                    List<String> pairwiseResults = Arrays.asList(JSONUtil.mapper.readValue(allPairwise, String[].class));

                    List<ListPairwiseResult> filteredPairwiseResults = pairwiseResults.stream()
                            .map(wrapException(pairwiseResult -> JSONUtil.mapper.readValue(pairwiseResult, ListPairwiseResult.class)))
                            .filter(wrapPredicateException((ListPairwiseResult result) -> result.getParsedMetadata().getTheirKey().equals(theirKey)))
                            .collect(Collectors.toList());


                    if (filteredPairwiseResults.size() == 1) {
                        return filteredPairwiseResults.get(0);
                    }
                    else {
                        throw new RuntimeException("Unexpected number of results when looking for pairwise by key");
                    }
                }));
    }

    CompletableFuture<String> getKeyForDid(String did) throws IndyException {
                log.debug("{} Called getKeyForDid: {}", name, did);
                return Did.keyForDid(pool.getPool(), wallet.getWallet(), did);
    }


    private CompletableFuture<EncryptedMessage> anoncrypt(CompletableFuture<? extends AnonCryptable> messageFuture) throws JsonProcessingException, IndyException {
        return messageFuture.thenCompose(wrapException(
                (AnonCryptable message) -> {
                    log.debug("{} Anoncrypting message: {}, with key: {}", name, message.toJSON(), message.getTheirKey());
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
                    log.debug("{} Authcrypting message: {}, myKey: {}, theirKey: {}", name, message.toJSON(), message.getMyKey(), message.getTheirKey());
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
