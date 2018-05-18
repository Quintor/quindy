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


    public CompletableFuture<CredentialRequest> createCredentialRequest(String theirDid, CredentialOffer claimOffer) throws IndyException, JsonProcessingException {
        return getPairwiseByTheirDid(theirDid)
                .thenCompose(wrapException(pairwiseResult -> getSchema(pairwiseResult.getMyDid(), claimOffer.getSchemaId())
                        .thenCompose(wrapException(schema -> getClaimDef(pairwiseResult.getMyDid(), claimOffer.getCredDefId())))
                        .thenCompose(wrapException(claimDefJson -> {
                            log.debug("{} creating claim request with claimDefJson {}", name, claimDefJson);
                            return Anoncreds.proverCreateCredentialReq(wallet.getWallet(), pairwiseResult.getMyDid(),
                                    claimOffer.toJSON(), claimDefJson.toJSON(), this.masterSecretName);
                        }))
                        .thenApply(proverCreateCredentialRequestResult -> {
                           log.debug("{} created credential request. Json: {}, metadata {} ", name, proverCreateCredentialRequestResult.getCredentialRequestJson(), proverCreateCredentialRequestResult.getCredentialRequestMetadataJson());
                           return new CredentialRequest(proverCreateCredentialRequestResult.getCredentialRequestJson(), proverCreateCredentialRequestResult.getCredentialRequestMetadataJson(), claimOffer, theirDid);
                        })
                ));
    }

    public CompletableFuture<CredentialRequest> createCredentialRequest(CredentialOffer claimOffer) throws IndyException, JsonProcessingException {
        return createCredentialRequest(claimOffer.getTheirDid(), claimOffer);
    }

    /**
     * Proves the proofRequest using the stored claims
     *
     * @param proofRequest
     * @param attributes   This map is used to get the correct claim, if multiple referents are present, or to provide self-attested attributes
     * @return
     */
    public CompletableFuture<Proof> fulfillProofRequest(ProofRequest proofRequest, Map<String, String> attributes) throws JsonProcessingException, IndyException {
        log.debug("{} Proving proof request: {}", name, proofRequest.toJSON());

        return Anoncreds.proverGetCredentialsForProofReq(wallet.getWallet(), proofRequest.toJSON())
                .thenApply(wrapException(claimsForProofRequestJson -> {
                    log.debug("{}: Obtained claims for proof request {}", name, claimsForProofRequestJson);
                    return JSONUtil.mapper.readValue(claimsForProofRequestJson, ClaimsForRequest.class);
                }))
                .thenCompose(wrapException(claimsForRequest -> createProofFromClaims(proofRequest, claimsForRequest, attributes, proofRequest.getTheirDid())))
                .thenApply(wrapException(proof -> {
                    log.debug("{}: Created proof {}", name, proof.toJSON());
                    return proof;
                }));
    }

    CompletableFuture<Proof> createProofFromClaims(ProofRequest proofRequest, ClaimsForRequest claimsForRequest, Map<String, String> attributes, String theirDid) throws JsonProcessingException {
        log.debug("{} Creating proof using claims: {}", name, claimsForRequest.toJSON());

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

        // Collect all claim referents needed for the proof
        Map<String, CredentialReferent> claimsByReferentKey = findNeededClaimReferents(proofRequest, claimsForRequest, attributes);

        // Store the claim referents in a way that enables us to fetch the needed entities from the ledger.
        Map<String, CredentialIdentifier> claimsForProof = claimsByReferentKey.values()
                .stream()
                .distinct()
                .collect(Collectors.toMap(referent -> referent.getCredentialInfo().getReferent(), CredentialIdentifier::new));

        // Create the json needed for creating the proof
        JsonNode requestedCredentials = createRequestedClaimsJson(proofRequest, selfAttestedAttributes, claimsByReferentKey);

        log.debug("{} gathered requestedCredentials: {}", name, requestedCredentials);

        // Fetch the required schema's and claim definitions, then create, the proof, deserialize and return it.
        return getEntitiesFromLedger(claimsForProof).thenCompose(wrapException(entities -> {
            log.debug("{} Creating proof with entities {}", name, entities);
            log.debug("{} Using schema's {}", name, JSONUtil.mapper
                    .writeValueAsString(entities.getSchemas()));
            return Anoncreds.proverCreateProof(wallet.getWallet(), proofRequest.toJSON(), JSONUtil.mapper.writeValueAsString(requestedCredentials), masterSecretName, JSONUtil.mapper
                    .writeValueAsString(entities.getSchemas()), JSONUtil.mapper.writeValueAsString(entities
                    .getClaimDefs()), "{}");
        }))
                .thenApply(wrapException(proofJson -> {
                    log.debug("{} Obtained proof: {}", name, proofJson);
                    Proof proof = JSONUtil.mapper.readValue(proofJson, Proof.class);
                    proof.setTheirDid(theirDid);
                    return proof;
                }));
    }

    private Map<String, CredentialReferent> findNeededClaimReferents(ProofRequest proofRequest, ClaimsForRequest claimsForRequest, Map<String, String> attributes) {
        // We find all the ClaimReferents that we are going to use. The cases:
        // 1. The referent is for an attribute and a value is provided -> Find any that matches the provided value
        // 2. The referent is for an attribute and no value is provided -> Find any
        // 3. The referent is for a predicate -> Find any

        return Stream.<Optional<AbstractMap.SimpleEntry<String, CredentialReferent>>>concat(claimsForRequest.getAttrs()
                .entrySet()
                .stream()
                .map((claimReferentEntry) -> {
                    return claimReferentEntry.getValue()
                            .stream()
                            .filter(credentialReferent -> credentialReferent.getCredentialInfo().getAttrs()
                                    .entrySet()
                                    .stream()
                                    // Find attribute that matches the one that is requested for this particular referent
                                    .filter(entry -> entry.getKey()
                                            .equals(proofRequest.getRequestedAttributes()
                                                    .get(claimReferentEntry.getKey())
                                                    .getName()))
                                    // Check if it matches the provided value, or the value is not provided
                                    .anyMatch(entry -> entry.getValue()
                                            .equals(attributes.getOrDefault(entry.getKey(), entry.getValue()))))
                            .map(credentialReferent -> new AbstractMap.SimpleEntry<>(claimReferentEntry.getKey(), credentialReferent))
                            .findAny();
                }), claimsForRequest.getPredicates()
                .entrySet()
                .stream()
                .map(entry -> entry.getValue()
                        .isEmpty() ? Optional.empty() : Optional.of(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()
                        .get(0))))).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private JsonNode createRequestedClaimsJson(ProofRequest proofRequest, Map<String, String> selfAttestedAttributes, Map<String, CredentialReferent> claimsByReferentKey) {
        Map<String, ProvingCredentialKey> requestedAttributes = proofRequest.getRequestedAttributes()
                .entrySet()
                .stream()
                .filter(stringAttributeInfoEntry -> stringAttributeInfoEntry.getValue()
                        .getRestrictions()
                        .isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        new ProvingCredentialKey(claimsByReferentKey.get(entry.getKey()).getCredentialInfo().getReferent(), Optional.of(true))));

        Map<String, ProvingCredentialKey> requestedPredicates = proofRequest.getRequestedPredicates()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ProvingCredentialKey(claimsByReferentKey.get(entry.getKey())
                        .getCredentialInfo().getReferent(), Optional.empty())));

        ObjectNode requestedClaimsJson = JSONUtil.mapper.createObjectNode();

        requestedClaimsJson.set("self_attested_attributes", JSONUtil.mapper.valueToTree(selfAttestedAttributes));
        requestedClaimsJson.set("requested_attributes", JSONUtil.mapper.valueToTree(requestedAttributes));
        requestedClaimsJson.set("requested_predicates", JSONUtil.mapper.valueToTree(requestedPredicates));
        return requestedClaimsJson;
    }


    public CompletableFuture<String> storeCredential(CredentialWithRequest credentialWithRequest) throws JsonProcessingException, IndyException {
        Credential credential = credentialWithRequest.getCredential();
        return getClaimDef(wallet.getMainDid(), credential.getCredDefId())
                .thenCompose(wrapException(
                        credentialDef -> Anoncreds.proverStoreCredential(wallet.getWallet(), null, credentialWithRequest.getCredentialRequest().getMetadata(), credential.toJSON(), credentialDef.toJSON(), null
                        ))
                ).thenApply(returnValue -> {
                    log.debug("{} return from storeCredential: {}", name, returnValue);
                    return returnValue;
                });
    }

    public CompletableFuture<List<CredentialInfo>> findAllClaims() throws IndyException {
        String filter = "{}";
        return Anoncreds.proverGetCredentials(wallet.getWallet(), filter)
                .thenApply(wrapException(this::deserializeClaimInfo));
    }


    private List<CredentialInfo> deserializeClaimInfo(String json) throws IOException {
        return JSONUtil.mapper.readValue(json, new TypeReference<List<CredentialInfo>>() {
        });
    }


}
