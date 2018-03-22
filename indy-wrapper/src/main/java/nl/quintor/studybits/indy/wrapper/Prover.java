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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class Prover extends WalletOwner {
    private String masterSecretName;

    public Prover(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }

    public void init(String masterSecretName) throws IndyException, ExecutionException, InterruptedException {
        this.masterSecretName = masterSecretName;
        Anoncreds.proverCreateMasterSecret(wallet.getWallet(), masterSecretName).get();
    }


    public CompletableFuture<ClaimRequest> storeClaimOfferAndCreateClaimRequest(ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return storeClaimOffer(claimOffer)
                        .thenCompose(wrapException((_void) -> createClaimRequest(claimOffer.getTheirDid(), claimOffer)));

    }

    CompletableFuture<ClaimRequest> createClaimRequest(String theirDid, ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return getPairwiseByTheirDid(theirDid)
                .thenCompose(wrapException(pairwiseResult -> getSchema(pairwiseResult.getMyDid(), claimOffer.getSchemaKey())
                        .thenCompose(wrapException(schema -> getClaimDef(pairwiseResult.getMyDid(), schema, claimOffer.getIssuerDid())))
                        .thenCompose(wrapException(claimDefJson -> {
                            log.debug("{} creating claim request with claimDefJson {}", name, claimDefJson);
                            return Anoncreds.proverCreateAndStoreClaimReq(wallet.getWallet(), pairwiseResult.getMyDid(),
                                    claimOffer.toJSON(), claimDefJson, this.masterSecretName)
                                    .thenCompose(wrapException(claimReqJsonStorageResponse -> {
                                        log.debug("{} Got claim request storage response {}", name, claimReqJsonStorageResponse);
                                        return Anoncreds.proverCreateAndStoreClaimReq(wallet.getWallet(), pairwiseResult.getMyDid(),
                                                claimOffer.toJSON(), claimDefJson, this.masterSecretName);
                                    }));
                        })).thenApply(wrapException(claimRequestJson -> {
                            ClaimRequest claimRequest = JSONUtil.mapper.readValue(claimRequestJson, ClaimRequest.class);
                            claimRequest.setTheirDid(theirDid);
                            return claimRequest;
                        })))

                );
    }

    CompletableFuture<Void> storeClaimOffer(ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return Anoncreds.proverStoreClaimOffer(wallet.getWallet(), claimOffer.toJSON());
    }

    /**
     * Proves the proofRequest using the stored claims
     *
     * @param proofRequest
     * @param attributes This map is used to get the correct claim, if multiple referents are present, or to provide self-attested attributes
     * @return
     */
    public CompletableFuture<Proof> fulfillProofRequest(ProofRequest proofRequest, Map<String, String> attributes) throws JsonProcessingException, IndyException {
        return Anoncreds.proverGetClaimsForProofReq(wallet.getWallet(), proofRequest.toJSON())
                .thenApply(wrapException(claimsForProofRequestJson -> {
                    return JSONUtil.mapper.readValue(claimsForProofRequestJson, ClaimsForRequest.class);
                }))
                .thenCompose(wrapException(claimsForRequest -> createProofFromClaims(proofRequest, claimsForRequest, attributes, proofRequest.getTheirDid())));

    }

    CompletableFuture<Proof> createProofFromClaims(ProofRequest proofRequest, ClaimsForRequest claimsForRequest, Map<String, String> attributes, String theirDid) throws JsonProcessingException {
        log.debug("{} Creating proof using claims: {}", name, claimsForRequest.toJSON());

        // Collect the names and values of all self-attested attributes. Throw an exception if one is not specified.
        Map<String, String> selfAttestedAttributes = proofRequest.getRequestedAttrs()
                .entrySet().stream()
                .filter(stringAttributeInfoEntry -> !stringAttributeInfoEntry.getValue().getRestrictions().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    String value = attributes.get(entry.getValue().getName());
                    if (value == null) {
                        throw new IllegalArgumentException("Self attested attribute was not provided");
                    }
                    return value;
                }));

        // Collect all claim referents needed for the proof
        Map<String, ClaimReferent> claimsByReferentKey = findNeededClaimReferents(proofRequest, claimsForRequest, attributes);

        // Store the claim referents in a way that enables us to fetch the needed entities from the ledger.
        Map<String, ClaimReferent> claimsForProof = claimsByReferentKey
                .values().stream()
                .distinct()
                .collect(Collectors.toMap(ClaimReferent::getReferent, Function.identity()));

        // Create the json needed for creating the proof
        JsonNode requestedClaimsJson = createRequestedClaimsJson(proofRequest, selfAttestedAttributes, claimsByReferentKey);

        // Fetch the required schema's and claim definitions, then create, the proof, deserialize and return it.
        return getEntitiesFromLedger(claimsForProof)
                .thenCompose(wrapException(entities -> {
                    log.debug("{} Creating proof", name);
                    return Anoncreds.proverCreateProof(wallet.getWallet(), proofRequest.toJSON(), JSONUtil.mapper.writeValueAsString(requestedClaimsJson),
                            JSONUtil.mapper.writeValueAsString(entities.getSchemas()), masterSecretName,
                            JSONUtil.mapper.writeValueAsString(entities.getClaimDefs()), "{}");
                }))
                .thenApply(wrapException(proofJson -> {
                    Proof proof = JSONUtil.mapper.readValue(proofJson, Proof.class);
                    proof.setTheirDid(theirDid);
                    return proof;
                }));
    }

    private Map<String, ClaimReferent> findNeededClaimReferents(ProofRequest proofRequest, ClaimsForRequest claimsForRequest, Map<String, String> attributes) {
        // We find all the ClaimReferents that we are going to use. The cases:
        // 1. The referent is for an attribute and a value is provided -> Find any that matches the provided value
        // 2. The referent is for an attribute and no value is provided -> Find any
        // 3. The referent is for a predicate -> Find any

        return Stream.<Optional<AbstractMap.SimpleEntry<String, ClaimReferent>>>concat(claimsForRequest
                        .getAttrs().entrySet()
                        .stream()
                        .map((claimReferentEntry) -> {
                            return claimReferentEntry.getValue()
                                    .stream()
                                    .filter(claimReferent -> claimReferent.getAttrs()
                                            .entrySet().stream()
                                            // Find attribute that matches the one that is requested for this particular referent
                                            .filter(entry -> entry.getKey().equals(proofRequest.getRequestedAttrs().get(claimReferentEntry.getKey()).getName()))
                                            // Check if it matches the provided value, or the value is not provided
                                            .anyMatch(entry -> entry.getValue().equals(attributes.getOrDefault(entry.getKey(), entry.getValue()))))
                                    .map(claimReferent -> new AbstractMap.SimpleEntry<>(claimReferentEntry.getKey(), claimReferent))
                                    .findAny();
                        }),
                claimsForRequest.getPredicates().entrySet().stream().map(entry -> entry.getValue().isEmpty() ?
                        Optional.empty() : Optional.of(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().get(0)))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private JsonNode createRequestedClaimsJson(ProofRequest proofRequest, Map<String, String> selfAttestedAttributes, Map<String, ClaimReferent> claimsByReferentKey) {
        Map<String, List<Object>> requestedAttributes = proofRequest.getRequestedAttrs()
                .entrySet().stream()
                .filter(stringAttributeInfoEntry -> stringAttributeInfoEntry.getValue().getRestrictions().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.asList(claimsByReferentKey.get(entry.getKey()).getReferent(), true)));

        Map<String, String> requestedPredicates = proofRequest.getRequestedPredicates()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> claimsByReferentKey.get(entry.getKey()).getReferent()));

        ObjectNode requestedClaimsJson = JSONUtil.mapper.createObjectNode();

        requestedClaimsJson.set("self_attested_attributes", JSONUtil.mapper.valueToTree(selfAttestedAttributes));
        requestedClaimsJson.set("requested_attrs", JSONUtil.mapper.valueToTree(requestedAttributes));
        requestedClaimsJson.set("requested_predicates", JSONUtil.mapper.valueToTree(requestedPredicates));
        return requestedClaimsJson;
    }


    public CompletableFuture<Void> storeClaim(Claim claim) throws JsonProcessingException, IndyException {
        return Anoncreds.proverStoreClaim(wallet.getWallet(), claim.toJSON(), null);
    }

    public CompletableFuture<List<ClaimInfo>> findAllClaims() throws IndyException {
        String filter = "{}";
        return Anoncreds.proverGetClaims(wallet.getWallet(), filter)
                .thenApply(wrapException(this::deserializeClaimInfo));
    }


    private List<ClaimInfo> deserializeClaimInfo(String json) throws IOException {
        return JSONUtil.mapper.readValue(json, new TypeReference<List<ClaimInfo>>(){});
    }



}
