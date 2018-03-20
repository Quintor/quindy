package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

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

    CompletableFuture<String> submitRequest(String request) throws IndyException {
        return Ledger.submitRequest(pool.getPool(), request);
    }

    CompletableFuture<String> signAndSubmitRequest(String request) throws IndyException {
        return signAndSubmitRequest(request, wallet.getMainDid());
    }

    CompletableFuture<String> signAndSubmitRequest(String request, String did) throws IndyException {
        return Ledger.signAndSubmitRequest(pool.getPool(), wallet.getWallet(), did, request);
    }

    public CompletableFuture<ConnectionResponse> acceptConnectionRequest(ConnectionRequest connectionRequest) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        log.debug("{} Called acceptConnectionRequest with {}, {}, {}", name, pool.getPool(), wallet.getWallet(), connectionRequest);

        return wallet.newDid()
                .thenApply(
                        (myDid) -> new ConnectionResponse(myDid.getDid(), myDid.getVerkey(), connectionRequest.getNonce(), connectionRequest.getDid()))
                .thenCompose(wrapException((ConnectionResponse connectionResponse) ->
                        getKeyForDid(connectionRequest.getDid())
                        .thenCompose(wrapException(key -> storeDidAndPairwise(connectionResponse.getDid(), connectionRequest.getDid(), connectionResponse.getVerkey(), key)))
                        .thenApply((_void) -> connectionResponse)));
    }

    CompletableFuture<Void> storeDidAndPairwise(String myDid, String theirDid, String myKey, String theirKey)  throws JsonProcessingException, IndyException {
        log.debug("{} Called storeDidAndPairwise: myDid: {}, theirDid: {}", name, myDid, theirDid);

        return Did.storeTheirDid(wallet.getWallet(), new TheirDidInfo(theirDid, theirKey).toJSON())
                .thenCompose(wrapException(
                        (storeDidResponse) -> {
                            log.debug("{} Creating pairwise theirDid: {}, myDid: {}, metadata: {}", name, theirDid, myDid, "");
                            return Pairwise.createPairwise(wallet.getWallet(), theirDid, myDid,"");

                        }));
    }

    public CompletableFuture<GetPairwiseResult> getPairwiseByTheirDid(String theirDid) throws IndyException {
        log.debug("{} Called getPairwise by their did: {}", name, theirDid);
        return Pairwise.getPairwise(wallet.getWallet(), theirDid)
                .thenApply(wrapException(json -> JSONUtil.mapper.readValue(json, GetPairwiseResult.class)));
    }

    CompletableFuture<String> getKeyForDid(String did) throws IndyException {
                log.debug("{} Called getKeyForDid: {}", name, did);
                return Did.keyForDid(pool.getPool(), wallet.getWallet(), did)
                        .thenApply(key -> {
                            log.debug("{} Got key for did {} key {}", name, did, key);
                            return key;
                        });

    }

    CompletableFuture<Schema> getSchema(String did, SchemaKey schemaKey) throws JsonProcessingException, IndyException {
        log.debug("{}: Calling buildGetSchemaRequest with submitter: {} destination {} GetSchema {}", name, did, schemaKey.getDid(), GetSchema.fromSchemaKey(schemaKey).toJSON());
        return Ledger.buildGetSchemaRequest(did, schemaKey.getDid(), GetSchema.fromSchemaKey(schemaKey).toJSON())
                .thenCompose(wrapException(this::submitRequest))
                .thenApply(wrapException(getSchemaResponse -> {
                    log.debug("{}: Got schema {} for schemaKey {}", name, getSchemaResponse, schemaKey);
                    return JSONUtil.readObjectByPointer(getSchemaResponse, "/result", Schema.class);
                }));
    }

    CompletableFuture<String> getClaimDef(String did, Schema schema, String issuerDid) throws IndyException {
        log.debug("{} Getting claim def with did {} schema with seqNo {} and issuerDid {}", name, did, schema.getSeqNo(), issuerDid);
        return Ledger.buildGetClaimDefTxn(did, schema.getSeqNo(), "CL", issuerDid)
                .thenCompose(wrapException(request -> {
                    log.debug("{} Submitting GetClaimDefTxn {}", name, request);
                    return submitRequest(request);
                }))
                .thenApply(wrapException(response -> JSONUtil.mapper.readTree(response).at("/result").toString()));
    }

    CompletableFuture<EntitiesFromLedger> getEntitiesFromLedger(Map<String, ClaimReferent> identifiers) {
        List<CompletableFuture<EntityFromLedger>> entityFutures = identifiers.entrySet().stream()
                .map(wrapException((Map.Entry<String, ClaimReferent> stringClaimReferentEntry) ->
                        getSchema(wallet.getMainDid(), stringClaimReferentEntry.getValue().getSchemaKey())
                                .thenCompose(wrapException((Schema schema) ->
                                        getClaimDef(wallet.getMainDid(), schema, stringClaimReferentEntry.getValue().getIssuerDid())
                                                .thenApply(claimDef -> new EntityFromLedger(schema, claimDef, stringClaimReferentEntry.getKey()))

                                ))))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(entityFutures.toArray(new CompletableFuture[0]))
                .thenApply(_void -> entityFutures.stream().map(CompletableFuture::join)
                .collect(EntitiesFromLedger.collector()));
    }

    public CompletableFuture<AnoncryptedMessage> anoncrypt(AnonCryptable message) throws JsonProcessingException, IndyException {
        log.debug("{} Anoncrypting message: {}, with did: {}", name, message.toJSON(), message.getTheirDid());
        return getKeyForDid(message.getTheirDid())
                .thenCompose(wrapException((String key) -> {
                    log.debug("{} Anoncrypting with key: {}", name, key);
                    return Crypto.anonCrypt(key, message.toJSON().getBytes(Charset.forName("utf8")))
                            .thenApply((byte[] cryptedMessage) -> new AnoncryptedMessage(cryptedMessage, message.getTheirDid()));
                }));
    }

    public <T extends AnonCryptable> CompletableFuture<T> anonDecrypt(AnoncryptedMessage message, Class<T> valueType) throws IndyException {
        return getKeyForDid(message.getTargetDid())
                .thenCompose(wrapException(key -> Crypto.anonDecrypt(wallet.getWallet(), key, message.getMessage())))
                .thenApply(wrapException((decryptedMessage) -> JSONUtil.mapper.readValue(new String(decryptedMessage, Charset.forName("utf8")), valueType)));
    }

    public CompletableFuture<AuthcryptedMessage> authcrypt(AuthCryptable message) throws JsonProcessingException, IndyException {
        log.debug("{} Authcrypting message: {}, theirDid: {}", name, message.toJSON(), message.getTheirDid());
        return getKeyForDid(message.getTheirDid())
                .thenCompose(wrapException((String theirKey) -> {
                    return getPairwiseByTheirDid(message.getTheirDid())
                            .thenCompose(wrapException((GetPairwiseResult getPairwiseResult) -> getKeyForDid(getPairwiseResult.getMyDid())
                                    .thenCompose(wrapException((String myKey) -> {
                                                log.debug("{} Authcrypting with keys myKey {}, theirKey {}", name, myKey, theirKey);
                                                return Crypto.authCrypt(wallet.getWallet(), myKey, theirKey, message.toJSON().getBytes(Charset.forName("utf8")))
                                                        .thenApply(cryptedMessage -> new AuthcryptedMessage(cryptedMessage, getPairwiseResult.getMyDid()));
                                            })
                                    ))
                            );
                }));
    }

    public <T extends AuthCryptable> CompletableFuture<T> authDecrypt(AuthcryptedMessage message, Class<T> valueType) throws IndyException {
        return getPairwiseByTheirDid(message.getDid())
                .thenCompose(wrapException(pairwiseResult -> getKeyForDid(pairwiseResult.getMyDid())
                        .thenCompose(wrapException(key -> Crypto.authDecrypt(wallet.getWallet(), key, message.getMessage())
                        .thenApply(wrapException((decryptedMessage) -> {
                            assert decryptedMessage.getVerkey().equals(key);
                            T decryptedObject = JSONUtil.mapper.readValue(new String(decryptedMessage.getDecryptedMessage(), Charset.forName("utf8")), valueType);
                            decryptedObject.setTheirDid(message.getDid());
                            return decryptedObject;
                        }))))))
                ;
    }
}
