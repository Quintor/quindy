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
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapBiFunctionException;
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

    private Pool pool;

    private IndyWallet(String name, Pool pool) throws IndyException, ExecutionException, InterruptedException {
        String issuerWalletConfig = "{\"id\":\""+name+"Wallet\"}";
        String issuerWalletCredentials = "{\"key\":\""+name+"_wallet_key\"}";
        this.wallet = Wallet.openWallet(issuerWalletConfig, issuerWalletCredentials).get();

        this.name = name;
        this.pool = pool;
    }

    protected IndyWallet(String name, String mainDid, String mainKey, Pool pool, Wallet wallet) {
        this.name = name;
        this.pool = pool;
        this.mainDid = mainDid;
        this.mainKey = mainKey;
        this.wallet = wallet;
    }


    public static IndyWallet create(IndyPool pool, String name, String seed) throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        String issuerWalletConfig = "{\"id\":\""+name+"Wallet\"}";
        String issuerWalletCredentials = "{\"key\":\""+name+"_wallet_key\"}";
        Wallet.createWallet(issuerWalletConfig, issuerWalletCredentials).get();

        IndyWallet indyWallet = new IndyWallet(name, pool.getPool());

        DidResults.CreateAndStoreMyDidResult result = indyWallet.newDid(seed).get();
        indyWallet.mainDid = result.getDid();
        indyWallet.mainKey = result.getVerkey();

        return indyWallet;
    }

    public static IndyWallet open(IndyPool pool, String name, String did) throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        IndyWallet indyWallet = new IndyWallet(name, pool.getPool());

        indyWallet.mainDid = did;
        indyWallet.mainKey = indyWallet.getKeyForDid(did).get();

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
        return Ledger.submitRequest(pool, request);
    }

    CompletableFuture<String> signAndSubmitRequest(String request) throws IndyException {
        return signAndSubmitRequest(request, mainDid);
    }

    CompletableFuture<String> signAndSubmitRequest(String request, String did) throws IndyException {
        return Ledger.signAndSubmitRequest(pool, wallet, did, request);
    }

    public CompletableFuture<ConnectionResponse> acceptConnectionRequest(ConnectionRequest connectionRequest) throws JsonProcessingException, IndyException {
        log.debug("{} Called acceptConnectionRequest with {}, {}, {}", name, pool, wallet, connectionRequest);

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

        return WalletRecord.add(wallet, "pairwise", theirDid, myDid, "{}");
    }

    public CompletableFuture<GetPairwiseResult> getPairwiseByTheirDid(String theirDid) throws IndyException {
        log.debug("{} Called getPairwise by their did: {}", name, theirDid);
        return WalletRecord.get(wallet, "pairwise", theirDid, "{}")
                .thenApply(wrapException(result -> JSONUtil.mapper.readValue(result, nl.quintor.studybits.indy.wrapper.dto.WalletRecord.class)))
                .thenApply(result -> new GetPairwiseResult(result.getValue(), ""));
    }

    static String lastDid;
    private CompletableFuture<String> getKeyForDid(String did) throws IndyException {
        log.debug("{} Called getKeyForDid: {}", name, did);
        log.debug("{} Matches lastDid {}", name, did.equals(lastDid));
        lastDid = did;

        log.debug("{} calling keyForDid pool: {}, wallet: {}, did: {}", name, pool, wallet, did);
        return Did.keyForDid(pool, wallet, did)
                .thenApply(key -> {
                    log.debug("{} Got key for did {} key {}", name, did, key);
                    return key;
                });
    }

    public CompletableFuture<String> getEndpointForDid(String did) throws IndyException {
        return Did.getEndpointForDid(wallet, pool, did).thenApply(DidResults.EndpointForDidResult::getAddress);
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

    public CompletableFuture<AnoncryptedMessage> anonEncrypt(byte[] message, String theirDid) throws JsonProcessingException, IndyException {
        log.debug("{} Anoncrypting message: {}, with did: {}", name, message, theirDid);
        return getKeyForDid(theirDid)
                .thenApply(key -> {
                    log.debug("{} EXTRA LOGGING", name);
                    return key;
                })
                .thenCompose(wrapException((String key) -> {
                    log.debug("{} Anoncrypting with key: {}", name, key);
                    return Crypto.anonCrypt(key, message)
                            .thenApply((byte[] cryptedMessage) -> new AnoncryptedMessage(cryptedMessage, theirDid));
                }));
    }

    public CompletableFuture<AnoncryptedMessage> anonEncrypt(AnonCryptable message) throws JsonProcessingException, IndyException {
        return anonEncrypt(message.toJSON().getBytes(Charset.forName("utf8")), message.getTheirDid());
    }

    public <T> CompletableFuture<T> anonDecrypt(byte[] message, String myDid, Class<T> valueType) throws IndyException {
        log.debug("{} Called anonDecrypt", name);
        return getKeyForDid(myDid)
                .thenCompose(wrapException(key -> Crypto.anonDecrypt(wallet, key, message)))
                .thenApply(wrapException((decryptedMessage) -> JSONUtil.mapper.readValue(new String(decryptedMessage, Charset
                        .forName("utf8")), valueType)));
    }

    public <T> CompletableFuture<T> anonDecrypt(AnoncryptedMessage message, Class<T> valueType) throws IndyException {
        return anonDecrypt(message.getMessage(), message.getTargetDid(), valueType);
    }

    public CompletableFuture<AuthcryptedMessage> authEncrypt(byte[] message, String theirDid) throws JsonProcessingException, IndyException {
        log.debug("{} Authcrypting message: {}, theirDid: {}", name, message, theirDid);
        return getKeyForDid(theirDid).thenCompose(wrapException((String theirKey) -> {
            return getPairwiseByTheirDid(theirDid)
                    .thenCompose(wrapException((GetPairwiseResult getPairwiseResult) -> getKeyForDid(getPairwiseResult.getMyDid())
                            .thenCompose(wrapException((String myKey) -> {
                                log.debug("{} Authcrypting with keys myKey {}, theirKey {}", name, myKey, theirKey);
                                return Crypto.authCrypt(wallet, myKey, theirKey, message)
                                        .thenApply(cryptedMessage -> new AuthcryptedMessage(cryptedMessage, getPairwiseResult.getMyDid()));
                            })))
                    );
        }));
    }

    public CompletableFuture<AuthcryptedMessage> authEncrypt(AuthCryptable message) throws JsonProcessingException, IndyException {
        return authEncrypt(message.toJSON().getBytes(Charset.forName("utf8")), message.getTheirDid());
    }

    public <T> CompletableFuture<T> authDecrypt(byte[] message, String theirDid, Class<T> valueType) throws IndyException {
        return getPairwiseByTheirDid(theirDid)
                .thenCompose(wrapException(pairwiseResult -> getKeyForDid(pairwiseResult.getMyDid())
                        .thenCompose(wrapException(key -> Crypto.authDecrypt(wallet, key, message)
                                .thenApply(wrapException((decryptedMessage) -> {
                                    T decryptedObject = JSONUtil.mapper.readValue(new String(decryptedMessage.getDecryptedMessage(), Charset
                                            .forName("utf8")), valueType);
                                    if (decryptedObject instanceof AuthCryptable) {
                                        ((AuthCryptable)decryptedObject).setTheirDid(theirDid);
                                    }
                                    return decryptedObject;
                                }))))))
                ;
    }

    public <T extends AuthCryptable> CompletableFuture<T> authDecrypt(AuthcryptedMessage message, Class<T> valueType) throws IndyException {
        return authDecrypt(message.getMessage(), message.getDid(), valueType);
    }

    @Override
    public void close() throws Exception {
        wallet.closeWallet().get();
    }

    public static void delete(String name) throws IndyException {
        Wallet.deleteWallet(name, null);
    }


}
