package nl.quintor.studybits.indy.wrapper.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.EntitiesFromLedger;
import nl.quintor.studybits.indy.wrapper.dto.Proof;
import nl.quintor.studybits.indy.wrapper.dto.ProofAttribute;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ProofUtils {

    public static CompletableFuture<List<ProofAttribute>> extractVerifiedProofAttributes(ProofRequest proofRequest, Proof proof, EntitiesFromLedger entitiesFromLedger) throws IndyException, JsonProcessingException {
        String proofRequestJson = proofRequest.toJSON();
        String proofJson = proof.toJSON();
        String schemaJson = JSONUtil.mapper.writeValueAsString(entitiesFromLedger.getSchemas());
        String credentialDefsJson = JSONUtil.mapper.writeValueAsString(entitiesFromLedger.getCredentialDefs());
        return Anoncreds
                .verifierVerifyProof(proofRequestJson, proofJson, schemaJson, credentialDefsJson, "{}", "{}")
                .thenAccept(result -> ValidateResult(result, "Invalid proof: verifierVerifyProof failed."))
                .thenApply(_void -> validateProofEncodings(proof))
                .thenAccept(result -> ValidateResult(result, "Invalid proof: encodings invalid"))
                .thenApply(_void -> extractProofAttributes(proofRequest, proof));
    }

    private static void ValidateResult(boolean valid, String message) throws IndyWrapperException {
        if (!valid) {
            throw new IndyWrapperException(message);
        }
    }

    private static boolean validateProofEncodings(Proof proof) {
        return proof
                .getRequestedProof()
                .getRevealedAttributes()
                .entrySet()
                .stream()
                .filter(entry -> !IntegerEncodingUtil.validateProofEncoding(entry.getValue()))
                .peek(entry -> log.error("Invalid proof received from theirDid '{}', entry '{}'", proof.getTheirDid(), entry))
                .count() == 0;
    }

    private static List<ProofAttribute> extractProofAttributes(ProofRequest proofRequest, Proof proof) {
        Map<String, String> attributeNameLookup = proofRequest
                .getRequestedAttributes()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getName()));

        // Get both the self attested and the revealed attributes
        Stream<ProofAttribute> selfAttestedStream = proof.getRequestedProof()
                .getSelfAttestedAttributes()
                .entrySet()
                .stream()
                .map(entry -> new ProofAttribute(entry.getKey(), attributeNameLookup.get(entry.getKey()), entry.getValue()));

        Stream<ProofAttribute> revealedStream = proof.getRequestedProof()
                .getRevealedAttributes()
                .entrySet()
                .stream()
                .map(entry -> new ProofAttribute(entry.getKey(), attributeNameLookup.get(entry.getKey()), entry.getValue().getRaw()));

        return Stream
                .concat(selfAttestedStream, revealedStream)
                .collect(Collectors.toList());
    }
}
