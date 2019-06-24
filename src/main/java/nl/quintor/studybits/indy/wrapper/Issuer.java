package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.IntegerEncodingUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapBiFunctionException;
import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.issuerCreateSchema;

@Slf4j
public class Issuer extends TrustAnchor {
    public Issuer(IndyWallet wallet) {
        super(wallet);
    }

    public CompletableFuture<String> createAndSendSchema( String schemaName, String schemaVersion, String... schemaAttributes ) throws IndyException {
        return issuerCreateSchema(getMainDid(), schemaName, schemaVersion, new JSONArray(schemaAttributes).toString())
        .thenCompose(wrapException(createSchemaResult -> createAndSendSchema(createSchemaResult)));
    }

    public CompletableFuture<String> createAndSendSchema( AnoncredsResults.IssuerCreateSchemaResult createSchemaResult ) throws IndyException {
        return Ledger.buildSchemaRequest(getMainDid(), createSchemaResult.getSchemaJson())
                     .thenCompose(wrapException(request -> {
                         log.debug("{}: Submitting buildSchema request {}", this.name, request);
                         return signAndSubmitRequest(request, getMainDid());
                     }))
                     .thenApply(requestResponse -> createSchemaResult.getSchemaId());
    }

    public CompletableFuture<String> defineCredential(String schemaId) throws JsonProcessingException, IndyException {
        log.debug("{}: Defining credential for schemaId {}", name, schemaId);
        return getSchema(getMainDid(), schemaId)
                .thenCompose(wrapException(schema ->
                {
                    // Remove special characters from the credential definition tag
                    String credDefTag = name + "_" + schemaId.replaceAll("[\\-\\+\\.\\^:,]","");
                    return Anoncreds.issuerCreateAndStoreCredentialDef(getWallet(), getMainDid(), schema.toJSON(), credDefTag, "CL", "{\"support_revocation\":false}");
                }))
                .thenCompose(wrapException(createAndStoreCredentialDefResult -> {
                    return Ledger.buildCredDefRequest(getMainDid(), createAndStoreCredentialDefResult.getCredDefJson())
                            .thenCompose(wrapException(credentialDefRequest -> {
                                log.debug("{} Signing and sending credentialDefRequest: {}", name, credentialDefRequest);
                                return Ledger.signAndSubmitRequest(getPool(), getWallet(), getMainDid(), credentialDefRequest)
                                        ;
                            })).thenApply((response) -> {
                                        log.debug("{} Got credentialDefRequest response: {}", name, response);
                                        return createAndStoreCredentialDefResult.getCredDefId();
                                    }
                            );
                }))
                .thenCompose(wrapException((String id) -> this.getCredentialDef(getMainDid(), id)))
                .thenApply(CredentialDefinition::getId);
    }


    public CompletableFuture<CredentialOffer> createCredentialOffer(String id, String targetDid) throws JsonProcessingException, IndyException {
        log.debug("{}: Creating credential offer with credentialdef id {}", id);
        return Anoncreds.issuerCreateCredentialOffer(getWallet(), id)
                .thenCombine(getPairwiseByTheirDid(targetDid),
                        wrapBiFunctionException((credentialOfferJson, pairwiseResult) -> {
                            log.debug("{} Created credentialOffer: {}", name, credentialOfferJson);
                            CredentialOffer credentialOffer = JSONUtil.mapper.readValue(credentialOfferJson, CredentialOffer.class);
                            log.debug("{} Created credentialOffer object (toJSON()): {}", name, credentialOffer.toJSON());
                            return credentialOffer;
                        }));
    }

    public CompletableFuture<CredentialWithRequest> createCredential(CredentialRequest credentialRequest, Map<String, Object> values) throws UnsupportedEncodingException, JsonProcessingException, IndyException {
        JsonNode credentialValueJson = IntegerEncodingUtil.credentialValuesFromMap(values);
        log.debug("{} Creating credential for: credentialOffer {}, claimRequest {}", name, credentialRequest.getCredentialOffer().toJSON(), credentialRequest.getRequest());
        return Anoncreds.issuerCreateCredential(getWallet(), credentialRequest.getCredentialOffer().toJSON(), credentialRequest.getRequest(), credentialValueJson.toString(), null, -1)
                .thenApply(wrapException((issuerCreateCredentialResult) -> {
                    log.debug("{} Created credential json: {}", name, issuerCreateCredentialResult.getCredentialJson());
                    Credential credential = JSONUtil.mapper.readValue(issuerCreateCredentialResult.getCredentialJson(), Credential.class);
                    return new CredentialWithRequest(credential, credentialRequest);
                }));
    }


}
