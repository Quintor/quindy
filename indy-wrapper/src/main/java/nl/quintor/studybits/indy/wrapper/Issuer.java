package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        SchemaDefinition schemaDefinition = new SchemaDefinition(name, version, Arrays.asList(attrNames));
        SchemaKey schemaKey = SchemaKey.fromSchema(schemaDefinition, issuerDid);

        log.debug("{}: Creating schemaDefinition: {} with did: {}", this.name, schemaDefinition.toJSON(), issuerDid);

        return Ledger.buildSchemaRequest(issuerDid, schemaDefinition.toJSON())
                .thenCompose(wrapException(request -> {
                    log.debug("{}: Submitting buildSchema request {}", this.name, request);
                    return signAndSubmitRequest(request, issuerDid);
                }))
                .thenApply(requestResponse -> schemaKey);
    }

    public CompletableFuture<String> defineClaim(SchemaKey schemaKey) throws JsonProcessingException, IndyException {
        log.debug("{}: Defining claim for schemaKey {}", name, schemaKey);
        return getSchema(issuerDid, schemaKey)
                .thenCompose(wrapException(schema ->
                        Anoncreds.issuerCreateAndStoreClaimDef(wallet.getWallet(), issuerDid, schema.toJSON(), "CL", false)))
                .thenCompose(wrapException(claimDefJson -> {
                    log.debug("{}: got claim def json {}", name, claimDefJson);
                    ClaimDefinition claimDefinition = JSONUtil.mapper.readValue(claimDefJson, ClaimDefinition.class);
                    log.debug("{}: building claim def txn with submitterDid {} xref {} signatureType {} data {}", name, issuerDid, claimDefinition.getRef(), claimDefinition.getSignatureType(), claimDefinition.getData().toString());
                    return Ledger.buildClaimDefTxn(issuerDid, claimDefinition.getRef(), claimDefinition.getSignatureType(), claimDefinition.getData().toString());
                })).thenCompose(wrapException(claimDefTxn -> {
                    log.debug("{} Signing and sending claimDefTx: {}", name, claimDefTxn);
                    return Ledger.signAndSubmitRequest(pool.getPool(), wallet.getWallet(), issuerDid, claimDefTxn)
                            ;
                })).thenApply((response) -> {
                            log.debug("{} Got ClaimDefTxn response: {}", name, response);
                            return response;
                        }
                );
    }

    public CompletableFuture<AuthcryptedMessage> createClaimOffer(SchemaKey schemaKey, String targetDid) throws JsonProcessingException, IndyException {
        return authcrypt(getSchema(issuerDid, schemaKey)
                .thenCompose(wrapException(schema -> Anoncreds.issuerCreateClaimOffer(wallet.getWallet(), schema.toJSON(), issuerDid, targetDid)))
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
