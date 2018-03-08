package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapBiFunctionException;
import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class Issuer extends TrustAnchor {
    @Getter
    private String issuerDid;
    @Getter
    private String issuerKey;

    public Issuer(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }

    public void init() throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        log.debug("{}: Initializing Issuer", name);
        DidResults.CreateAndStoreMyDidResult issuerDidAndKey = wallet.newDid()
                .thenCompose(wrapException(createAndStoreMyDidResult ->
                        sendNym(createAndStoreMyDidResult.getDid(), createAndStoreMyDidResult.getVerkey(), null)
                        .thenApply(sendNymResult -> createAndStoreMyDidResult))).get();


        issuerDid = issuerDidAndKey.getDid();
        issuerKey = issuerDidAndKey.getVerkey();

        log.debug("{}: Issuer initialized with did: {} and key: {}", name, issuerDid, issuerKey);
    }

    public CompletableFuture<SchemaKey> createAndSendSchema(String name, String version, String... attrNames) throws IndyException, JsonProcessingException {
        Schema schema = new Schema(name, version, Arrays.asList(attrNames));
        SchemaKey schemaKey = SchemaKey.fromSchema(schema, issuerDid);

        log.debug("{}: Creating schema: {} with did: {}", this.name, schema.toJSON(), issuerDid);

        return Ledger.buildSchemaRequest(issuerDid, schema.toJSON())
                .thenCompose(wrapException(request -> {
                    log.debug("{}: Submitting buildSchema request {}", this.name, request);
                    return signAndSubmitRequest(request, issuerDid);
                }))
                .thenApply(requestResponse -> schemaKey);
    }

    public CompletableFuture<String> defineClaim(SchemaKey schemaKey) throws JsonProcessingException, IndyException {
        log.debug("{}: Defining claim for schemaKey {}", name, schemaKey);
        return getSchema(schemaKey)
                .thenCompose(wrapException(schema ->
                        Anoncreds.issuerCreateAndStoreClaimDef(wallet.getWallet(), issuerDid, schema, "CL", false)))
        .thenCompose(wrapException(claimDefJson -> {
            JsonNode claimDef = JSONUtil.mapper.readTree(claimDefJson);

            return Ledger.buildClaimDefTxn(issuerDid, claimDef.get("ref").asInt(), claimDef.get("signature_type").toString(), claimDef.get("data").toString())
                    .thenCompose(wrapException(claimDefTxn -> Ledger.signAndSubmitRequest(pool.getPool(), wallet.getWallet(), issuerDid, claimDefTxn)));
        }));
    }

    CompletableFuture<String> getSchema(SchemaKey schemaKey) throws JsonProcessingException, IndyException {
        log.debug("{}: Calling buildGetSchemaRequest with submitter: {} destination {} GetSchema {}", name, issuerDid, schemaKey.getDid(), GetSchema.fromSchemaKey(schemaKey).toJSON());
        return Ledger.buildGetSchemaRequest(issuerDid, schemaKey.getDid(), GetSchema.fromSchemaKey(schemaKey).toJSON())
                .thenCompose(wrapException(request -> Ledger.submitRequest(pool.getPool(), request)))
                .thenApply(wrapException(getSchemaResponse -> {
                    log.debug("{}: Got schema {} for schemaKey {}", name, getSchemaResponse, schemaKey);
                    return JSONUtil.mapper.readTree(getSchemaResponse).at("/result").toString();
                }));
    }

    public CompletableFuture<AuthcryptedMessage> createClaimOffer(SchemaKey schemaKey, String targetDid) throws JsonProcessingException, IndyException {
        return authcrypt(getSchema(schemaKey)
                .thenCompose(wrapException(schema -> Anoncreds.issuerCreateClaimOffer(wallet.getWallet(), schema, issuerDid, targetDid)))
                .thenCombine(getPairwiseByTheirDid(targetDid),
                        wrapBiFunctionException((claimOfferJson, pairwiseResult) -> {
                            log.debug("{} Created claimOffer: {}", name, claimOfferJson);
                            ClaimOffer claimOffer = JSONUtil.mapper.readValue(claimOfferJson, ClaimOffer.class);
                            claimOffer.setMyDid(pairwiseResult.getMyDid());
                            claimOffer.setTheirDid(targetDid);
                            log.debug("{} Created claimOffer object (toJSON()): {}", name, claimOffer.toJSON());
                            return claimOffer;
                        })));
    }
}
