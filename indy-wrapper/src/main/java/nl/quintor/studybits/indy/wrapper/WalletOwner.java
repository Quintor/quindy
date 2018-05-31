package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.ProofUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pairwise.Pairwise;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class WalletOwner implements AutoCloseable {
    IndyPool pool;
    @Getter
    IndyWallet wallet;
    @Getter
    String name;

    public WalletOwner(String name, IndyPool pool, IndyWallet wallet) {
        this.name = name;
        this.pool = pool;
        this.wallet = wallet;
    }

    private CompletableFuture<String> submitRequest(String request) throws IndyException {
        return Ledger.submitRequest(pool.getPool(), request);
    }

    CompletableFuture<String> signAndSubmitRequest(String request) throws IndyException {
        return signAndSubmitRequest(request, wallet.getMainDid());
    }

    CompletableFuture<String> signAndSubmitRequest(String request, String did) throws IndyException {
        return Ledger.signAndSubmitRequest(pool.getPool(), wallet.getWallet(), did, request);
    }

    public CompletableFuture<ConnectionResponse> acceptConnectionRequest(ConnectionRequest connectionRequest) throws JsonProcessingException, IndyException {
        log.debug("{} Called acceptConnectionRequest with {}, {}, {}", name, pool.getPool(), wallet.getWallet(), connectionRequest);

        return wallet.newDid()
                .thenApply(
                        (myDid) -> new ConnectionResponse(myDid.getDid(), myDid.getVerkey(), connectionRequest.getNonce(), connectionRequest
                                .getDid()))
                .thenCompose(wrapException((ConnectionResponse connectionResponse) ->
                        getKeyForDid(connectionRequest.getDid())
                                .thenCompose(wrapException(key -> storeDidAndPairwise(connectionResponse.getDid(), connectionRequest.getDid(), key)))
                                .thenApply((_void) -> connectionResponse)));
    }

    CompletableFuture<Void> storeDidAndPairwise(String myDid, String theirDid, String theirKey) throws JsonProcessingException, IndyException {
        log.debug("{} Called storeDidAndPairwise: myDid: {}, theirDid: {}", name, myDid, theirDid);

        return Did.storeTheirDid(wallet.getWallet(), new TheirDidInfo(theirDid, theirKey).toJSON())
                .thenCompose(wrapException(
                        (storeDidResponse) -> {
                            log.debug("{} Creating pairwise theirDid: {}, myDid: {}, metadata: {}", name, theirDid, myDid, "");
                            return Pairwise.createPairwise(wallet.getWallet(), theirDid, myDid, "");
                        }));
    }

    public CompletableFuture<GetPairwiseResult> getPairwiseByTheirDid(String theirDid) throws IndyException {
        log.debug("{} Called getPairwise by their did: {}", name, theirDid);
        return Pairwise.getPairwise(wallet.getWallet(), theirDid)
                .thenApply(wrapException(json -> JSONUtil.mapper.readValue(json, GetPairwiseResult.class)));
    }


    private CompletableFuture<String> getKeyForDid(String did) throws IndyException {
        log.debug("{} Called getKeyForDid: {}", name, did);
        return Did.keyForDid(pool.getPool(), wallet.getWallet(), did)
                .thenApply(key -> {
                    log.debug("{} Got key for did {} key {}", name, did, key);
                    return key;
                });
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

    CompletableFuture<CredentialDefinition> getCredentialDefByNameAndKey(String did, String id) throws IndyException {
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
                .map(wrapException((Map.Entry<String, CredentialIdentifier> stringCredentialIdentifierEntry) -> getSchema(wallet.getMainDid(), stringCredentialIdentifierEntry
                        .getValue()
                        .getSchemaId()).thenCompose(wrapException((Schema schema) -> getCredentialDef(wallet.getMainDid(), stringCredentialIdentifierEntry
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
                .thenCompose(wrapException(key -> Crypto.anonDecrypt(wallet.getWallet(), key, message.getMessage())))
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
                                return Crypto.authCrypt(wallet.getWallet(), myKey, theirKey, message.toJSON()
                                        .getBytes(Charset.forName("utf8")))
                                        .thenApply(cryptedMessage -> new AuthcryptedMessage(cryptedMessage, getPairwiseResult.getMyDid()));
                            })))
                    );
        }));
    }

    public <T extends AuthCryptable> CompletableFuture<T> authDecrypt(AuthcryptedMessage message, Class<T> valueType) throws IndyException {
        return getPairwiseByTheirDid(message.getDid())
                .thenCompose(wrapException(pairwiseResult -> getKeyForDid(pairwiseResult.getMyDid())
                        .thenCompose(wrapException(key -> Crypto.authDecrypt(wallet.getWallet(), key, message.getMessage())
                                .thenApply(wrapException((decryptedMessage) -> {
                                    assert decryptedMessage.getVerkey().equals(key);
                                    T decryptedObject = JSONUtil.mapper.readValue(new String(decryptedMessage.getDecryptedMessage(), Charset
                                            .forName("utf8")), valueType);

                                    decryptedObject.setTheirDid(message.getDid());
                                    return decryptedObject;
                                }))))))
                ;
    }

    public CompletableFuture<List<ProofAttribute>> getVerifiedProofAttributes(ProofRequest proofRequest, Proof proof) {
        Map<String, CredentialIdentifier> identifierMap = proof.getIdentifiers()
                .stream()
                .collect(Collectors.toMap(CredentialIdentifier::getCredDefId, Function.identity()));
        return getEntitiesFromLedger(identifierMap)
                .thenCompose(wrapException(entitiesFromLedger -> ProofUtils.extractVerifiedProofAttributes(proofRequest, proof, entitiesFromLedger)));
    }

    @Override
    public void close() throws Exception {
        wallet.close();
    }
}
