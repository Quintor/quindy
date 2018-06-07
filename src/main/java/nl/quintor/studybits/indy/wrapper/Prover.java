package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class Prover extends WalletOwner {
    private String masterSecretName;

    public Prover(String name, IndyPool pool, IndyWallet wallet, String masterSecretName) {
        super(name, pool, wallet);
        this.masterSecretName = masterSecretName;
    }

    public void init() throws IndyException, ExecutionException, InterruptedException {
        Anoncreds.proverCreateMasterSecret(wallet.getWallet(), masterSecretName).get();
    }


    public CompletableFuture<CredentialRequest> createCredentialRequest(String theirDid, CredentialOffer credentialOffer) throws IndyException, JsonProcessingException {
        return getPairwiseByTheirDid(theirDid)
                .thenCompose(wrapException(pairwiseResult -> getSchema(pairwiseResult.getMyDid(), credentialOffer.getSchemaId())
                        .thenCompose(wrapException(schema -> getCredentialDef(pairwiseResult.getMyDid(), credentialOffer.getCredDefId())))
                        .thenCompose(wrapException(credentialDefJson -> {
                            log.debug("{} creating credential request with credentialDefJson {}", name, credentialDefJson);
                            return Anoncreds.proverCreateCredentialReq(wallet.getWallet(), pairwiseResult.getMyDid(),
                                    credentialOffer.toJSON(), credentialDefJson.toJSON(), this.masterSecretName);
                        }))
                        .thenApply(proverCreateCredentialRequestResult -> {
                           log.debug("{} created credential request. Json: {}, metadata {} ", name, proverCreateCredentialRequestResult.getCredentialRequestJson(), proverCreateCredentialRequestResult.getCredentialRequestMetadataJson());
                           return new CredentialRequest(proverCreateCredentialRequestResult.getCredentialRequestJson(), proverCreateCredentialRequestResult.getCredentialRequestMetadataJson(), credentialOffer, theirDid);
                        })
                ));
    }

    public CompletableFuture<CredentialRequest> createCredentialRequest(CredentialOffer credentialOffer) throws IndyException, JsonProcessingException {
        return createCredentialRequest(credentialOffer.getTheirDid(), credentialOffer);
    }

    /**
     * Proves the proofRequest using the stored credentials
     *
     * @param proofRequest
     * @param attributes   This map is used to get the correct credentials, if multiple referents are present, or to provide self-attested attributes
     * @return
     */
    public CompletableFuture<Proof> fulfillProofRequest(ProofRequest proofRequest, Map<String, String> attributes) throws JsonProcessingException, IndyException {
        log.debug("{} Proving proof request: {}", name, proofRequest.toJSON());

        return Anoncreds.proverGetCredentialsForProofReq(wallet.getWallet(), proofRequest.toJSON())
                .thenApply(wrapException(credentialsForProofReqJson -> {
                    log.debug("{}: Obtained credentials for proof request {}", name, credentialsForProofReqJson);
                    return JSONUtil.mapper.readValue(credentialsForProofReqJson, CredentialsForRequest.class);
                }))
                .thenCompose(wrapException(credentialsForRequest -> createProofFromCredentials(proofRequest, credentialsForRequest, attributes, proofRequest.getTheirDid())))
                .thenApply(wrapException(proof -> {
                    log.debug("{}: Created proof {}", name, proof.toJSON());
                    return proof;
                }));
    }

    CompletableFuture<Proof> createProofFromCredentials(ProofRequest proofRequest, CredentialsForRequest credentialsForRequest, Map<String, String> attributes, String theirDid) throws JsonProcessingException {
        log.debug("{} Creating proof using credentials: {}", name, credentialsForRequest.toJSON());

        // Collect the names and values of all self-attested attributes. Throw an exception if one is not specified.
        Map<String, String> selfAttestedAttributes = proofRequest.getRequestedAttributes()
                .entrySet()
                .stream()
                .filter(stringAttributeInfoEntry -> !stringAttributeInfoEntry.getValue()
                        .getRestrictions()
                        .isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    String value = attributes.get(entry.getValue().getName());
                    if (value == null) {
                        throw new IllegalArgumentException("Self attested attribute was not provided");
                    }
                    return value;
                }));

        // Collect all credential referents needed for the proof
        Map<String, CredentialReferent> credentialReferentMap = findNeededCredentialReferents(proofRequest, credentialsForRequest, attributes);

        // Store the credential referents in a way that enables us to fetch the needed entities from the ledger.
        Map<String, CredentialIdentifier> credentialsForProof = credentialReferentMap.values()
                .stream()
                .distinct()
                .collect(Collectors.toMap(referent -> referent.getCredentialInfo().getReferent(), CredentialIdentifier::new));

        // Create the json needed for creating the proof
        JsonNode requestedCredentials = createRequestedCredentialsJson(proofRequest, selfAttestedAttributes, credentialReferentMap);

        log.debug("{} gathered requestedCredentials: {}", name, requestedCredentials);

        // Fetch the required schema's and credential definitions, then create, the proof, deserialize and return it.
        return getEntitiesFromLedger(credentialsForProof).thenCompose(wrapException(entities -> {
            log.debug("{} Creating proof with entities {}", name, entities);
            log.debug("{} Using schema's {}", name, JSONUtil.mapper
                    .writeValueAsString(entities.getSchemas()));
            return Anoncreds.proverCreateProof(wallet.getWallet(), proofRequest.toJSON(), JSONUtil.mapper.writeValueAsString(requestedCredentials), masterSecretName, JSONUtil.mapper
                    .writeValueAsString(entities.getSchemas()), JSONUtil.mapper.writeValueAsString(entities
                    .getCredentialDefs()), "{}");
        }))
                .thenApply(wrapException(proofJson -> {
                    log.debug("{} Obtained proof: {}", name, proofJson);
                    Proof proof = JSONUtil.mapper.readValue(proofJson, Proof.class);
                    proof.setTheirDid(theirDid);
                    return proof;
                }));
    }

    private Map<String, CredentialReferent> findNeededCredentialReferents(ProofRequest proofRequest, CredentialsForRequest credentialsForRequest, Map<String, String> attributes) {
        // We find all the CredentialReferents that we are going to use. The cases:
        // 1. The referent is for an attribute and a value is provided -> Find any that matches the provided value
        // 2. The referent is for an attribute and no value is provided -> Find any
        // 3. The referent is for a predicate -> Find any

        return Stream.<Optional<AbstractMap.SimpleEntry<String, CredentialReferent>>>concat(credentialsForRequest.getAttrs()
                .entrySet()
                .stream()
                .map((credentialReferentEntry) -> {
                    return credentialReferentEntry.getValue()
                            .stream()
                            .filter(credentialReferent -> credentialReferent.getCredentialInfo().getAttrs()
                                    .entrySet()
                                    .stream()
                                    // Find attribute that matches the one that is requested for this particular referent
                                    .filter(entry -> entry.getKey()
                                            .equals(proofRequest.getRequestedAttributes()
                                                    .get(credentialReferentEntry.getKey())
                                                    .getName()))
                                    // Check if it matches the provided value, or the value is not provided
                                    .anyMatch(entry -> entry.getValue()
                                            .equals(attributes.getOrDefault(entry.getKey(), entry.getValue()))))
                            .map(credentialReferent -> new AbstractMap.SimpleEntry<>(credentialReferentEntry.getKey(), credentialReferent))
                            .findAny();
                }), credentialsForRequest.getPredicates()
                .entrySet()
                .stream()
                .map(entry -> entry.getValue()
                        .isEmpty() ? Optional.empty() : Optional.of(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()
                        .get(0))))).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private JsonNode createRequestedCredentialsJson(ProofRequest proofRequest, Map<String, String> selfAttestedAttributes, Map<String, CredentialReferent> credentialByReferentKey) {
        Map<String, ProvingCredentialKey> requestedAttributes = proofRequest.getRequestedAttributes()
                .entrySet()
                .stream()
                .filter(stringAttributeInfoEntry -> stringAttributeInfoEntry.getValue()
                        .getRestrictions()
                        .isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        new ProvingCredentialKey(credentialByReferentKey.get(entry.getKey()).getCredentialInfo().getReferent(), Optional.of(true))));

        Map<String, ProvingCredentialKey> requestedPredicates = proofRequest.getRequestedPredicates()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ProvingCredentialKey(credentialByReferentKey.get(entry.getKey())
                        .getCredentialInfo().getReferent(), Optional.empty())));

        ObjectNode requestedCredentialJson = JSONUtil.mapper.createObjectNode();

        requestedCredentialJson.set("self_attested_attributes", JSONUtil.mapper.valueToTree(selfAttestedAttributes));
        requestedCredentialJson.set("requested_attributes", JSONUtil.mapper.valueToTree(requestedAttributes));
        requestedCredentialJson.set("requested_predicates", JSONUtil.mapper.valueToTree(requestedPredicates));
        return requestedCredentialJson;
    }


    public CompletableFuture<String> storeCredential(CredentialWithRequest credentialWithRequest) throws JsonProcessingException, IndyException {
        Credential credential = credentialWithRequest.getCredential();
        return getCredentialDef(wallet.getMainDid(), credential.getCredDefId())
                .thenCompose(wrapException(
                        credentialDef -> Anoncreds.proverStoreCredential(wallet.getWallet(), null, credentialWithRequest.getCredentialRequest().getMetadata(), credential.toJSON(), credentialDef.toJSON(), null
                        ))
                ).thenApply(returnValue -> {
                    log.debug("{} return from storeCredential: {}", name, returnValue);
                    return returnValue;
                });
    }

    public CompletableFuture<List<CredentialInfo>> findAllCredentials() throws IndyException {
        String filter = "{}";
        return Anoncreds.proverGetCredentials(wallet.getWallet(), filter)
                .thenApply(wrapException(this::deserializeCredentialInfo));
    }


    private List<CredentialInfo> deserializeCredentialInfo(String json) throws IOException {
        return JSONUtil.mapper.readValue(json, new TypeReference<List<CredentialInfo>>() {
        });
    }


}
