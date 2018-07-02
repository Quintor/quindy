package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
@Getter
public class IndyWallet implements AutoCloseable {
    private Wallet wallet;
    protected String name;
    @Setter
    private String mainDid;
    @Setter
    private String mainKey;

    private LookupRepository lookupRepository;


    private IndyWallet(String name) throws IndyException, ExecutionException, InterruptedException {
        this.wallet = Wallet.openWallet(name, null, null).get();

        this.name = name;
    }

    protected IndyWallet(IndyWallet wallet) {
        this.name = wallet.getName();
        this.mainDid = wallet.getMainDid();
        this.mainKey = wallet.getMainKey();
        this.wallet = wallet.getWallet();
        this.lookupRepository = wallet.getLookupRepository();
    }

    public static IndyWallet create(LookupRepository lookupRepository, String poolName, String name, String seed) throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        Wallet.createWallet(poolName, name, "default", null, null).get();

        IndyWallet indyWallet = new IndyWallet(name);

        DidResults.CreateAndStoreMyDidResult result = indyWallet.newDid(seed).get();
        indyWallet.mainDid = result.getDid();
        indyWallet.mainKey = result.getVerkey();
        indyWallet.lookupRepository = lookupRepository;

        return indyWallet;
    }

    CompletableFuture<DidResults.CreateAndStoreMyDidResult> newDid() throws JsonProcessingException, IndyException {
        return newDid(null);
    }

    CompletableFuture<DidResults.CreateAndStoreMyDidResult> newDid(String seed) throws JsonProcessingException, IndyException {
        String seedJSON = StringUtils.isNotBlank(seed) ? (new MyDidInfo(seed)).toJSON() : "{}";
        log.debug("Creating new did with seedJSON: {}", seedJSON);
        return Did.createAndStoreMyDid(wallet, seedJSON);
    }

    private CompletableFuture<String> submitRequest(String request) throws IndyException {
        return lookupRepository.submitRequest(request);
    }

    CompletableFuture<String> signAndSubmitRequest(String request) throws IndyException {
        return signAndSubmitRequest(request, mainDid);
    }

    CompletableFuture<String> signAndSubmitRequest(String request, String did) throws IndyException {
        return lookupRepository.signAndSubmitRequest(request, did, wallet);
    }

    public CompletableFuture<ConnectionResponse> acceptConnectionRequest(ConnectionRequest connectionRequest) throws JsonProcessingException, IndyException {
        log.debug("{} Called acceptConnectionRequest with {}, {}", name, wallet, connectionRequest);

        return newDid()
                .thenApply(
                        (myDid) -> new ConnectionResponse(myDid.getDid(), myDid.getVerkey(), connectionRequest.getRequestNonce(), connectionRequest
                                .getDid()))
                .thenCompose(wrapException((ConnectionResponse connectionResponse) ->
                        getKeyForDid(connectionRequest.getDid())
                                .thenCompose(wrapException(key -> storeDidAndPairwise(connectionResponse.getDid(), connectionRequest.getDid(), key)))
                                .thenApply((_void) -> connectionResponse)));
    }

    CompletableFuture<Void> storeDidAndPairwise(String myDid, String theirDid, String theirKey) throws JsonProcessingException, IndyException {
        log.debug("{} Called storeDidAndPairwise: myDid: {}, theirDid: {}", name, myDid, theirDid);

        return Did.storeTheirDid(wallet, new TheirDidInfo(theirDid, theirKey).toJSON())
                .thenCompose(wrapException(
                        (storeDidResponse) -> {
                            log.debug("{} Creating pairwise theirDid: {}, myDid: {}, metadata: {}", name, theirDid, myDid, "");
                            return Pairwise.createPairwise(wallet, theirDid, myDid, "");
                        }));
    }

    public CompletableFuture<GetPairwiseResult> getPairwiseByTheirDid(String theirDid) throws IndyException {
        log.debug("{} Called getPairwise by their did: {}", name, theirDid);
        return Pairwise.getPairwise(wallet, theirDid)
                .thenApply(wrapException(json -> JSONUtil.mapper.readValue(json, GetPairwiseResult.class)));
    }


    private CompletableFuture<String> getKeyForDid(String did) throws IndyException {
        return lookupRepository.getKeyForDid(did, wallet);
    }

    public CompletableFuture<Schema> getSchema(String did, String schemaId) throws JsonProcessingException, IndyException {
        log.debug("{}: Calling buildGetSchemaRequest with submitter: {} schemaId {}", name, did, schemaId);
        return Ledger.buildGetSchemaRequest(did, schemaId)
                .thenCompose(wrapException(this::submitRequest))
                .thenCompose(wrapException(Ledger::parseGetSchemaResponse))
                .thenApply(wrapException(getSchemaResponse -> {
                    log.debug("{}: Got schema {} }", name, getSchemaResponse.getObjectJson());

                    return JSONUtil.mapper.readValue(getSchemaResponse.getObjectJson(), Schema.class);
                }));
    }

    CompletableFuture<CredentialDefinition> getCredentialDef(String did, String id) throws IndyException {
        log.debug("{} Getting credential def with did {} schema with id {}", name, did, id);
        return Ledger.buildGetCredDefRequest(did, id)
                .thenCompose(wrapException(request -> {
                    log.debug("{} Submitting GetCredDefRequest {}", name, request);
                    return submitRequest(request);
                }))
                .thenCompose(AsyncUtil.wrapException(Ledger::parseGetCredDefResponse))
                .thenApply(LedgerResults.ParseResponseResult::getObjectJson)
                .thenApply(AsyncUtil.wrapException(object -> JSONUtil.mapper.readValue(object, CredentialDefinition.class)));
    }

    CompletableFuture<EntitiesFromLedger> getEntitiesFromLedger(Map<String, CredentialIdentifier> identifiers) {
        List<CompletableFuture<EntitiesForCredentialReferent>> entityFutures = identifiers.entrySet()
                .stream()
                .map(wrapException((Map.Entry<String, CredentialIdentifier> stringCredentialIdentifierEntry) -> getSchema(mainDid, stringCredentialIdentifierEntry
                        .getValue()
                        .getSchemaId()).thenCompose(wrapException((Schema schema) -> getCredentialDef(mainDid, stringCredentialIdentifierEntry
                        .getValue()
                        .getCredDefId()).thenApply(credentialDef -> new EntitiesForCredentialReferent(schema, credentialDef, stringCredentialIdentifierEntry
                        .getKey()))))))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(entityFutures.toArray(new CompletableFuture[0]))
                .thenApply(_void -> entityFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(EntitiesFromLedger.collector()));
    }

    public CompletableFuture<AnoncryptedMessage> anonEncrypt(AnonCryptable message) throws JsonProcessingException, IndyException {
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
                .thenCompose(wrapException(key -> Crypto.anonDecrypt(wallet, key, message.getMessage())))
                .thenApply(wrapException((decryptedMessage) -> JSONUtil.mapper.readValue(new String(decryptedMessage, Charset
                        .forName("utf8")), valueType)));
    }

    public CompletableFuture<AuthcryptedMessage> authEncrypt(AuthCryptable message) throws JsonProcessingException, IndyException {
        log.debug("{} Authcrypting message: {}, theirDid: {}", name, message.toJSON(), message.getTheirDid());
        return getKeyForDid(message.getTheirDid()).thenCompose(wrapException((String theirKey) -> {
            return getPairwiseByTheirDid(message.getTheirDid())
                    .thenCompose(wrapException((GetPairwiseResult getPairwiseResult) -> getKeyForDid(getPairwiseResult.getMyDid())
                            .thenCompose(wrapException((String myKey) -> {
                                log.debug("{} Authcrypting with keys myKey {}, theirKey {}", name, myKey, theirKey);
                                return Crypto.authCrypt(wallet, myKey, theirKey, message.toJSON()
                                        .getBytes(Charset.forName("utf8")))
                                        .thenApply(cryptedMessage -> new AuthcryptedMessage(cryptedMessage, getPairwiseResult.getMyDid()));
                            })))
                    );
        }));
    }

    public <T extends AuthCryptable> CompletableFuture<T> authDecrypt(AuthcryptedMessage message, Class<T> valueType) throws IndyException {
        return getPairwiseByTheirDid(message.getDid())
                .thenCompose(wrapException(pairwiseResult -> getKeyForDid(pairwiseResult.getMyDid())
                        .thenCompose(wrapException(key -> Crypto.authDecrypt(wallet, key, message.getMessage())
                                .thenApply(wrapException((decryptedMessage) -> {
                                    T decryptedObject = JSONUtil.mapper.readValue(new String(decryptedMessage.getDecryptedMessage(), Charset
                                            .forName("utf8")), valueType);
                                    decryptedObject.setTheirDid(message.getDid());
                                    return decryptedObject;
                                }))))))
                ;
    }

    @Override
    public void close() throws Exception {
        wallet.closeWallet();
    }

    public static void delete(String name) throws IndyException {
        Wallet.deleteWallet(name, null);
    }


}
