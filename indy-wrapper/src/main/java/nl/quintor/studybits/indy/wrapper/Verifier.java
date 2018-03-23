package nl.quintor.studybits.indy.wrapper;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.Proof;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import nl.quintor.studybits.indy.wrapper.util.IntegerEncodingUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class Verifier extends WalletOwner {
    public Verifier( String name, IndyPool pool, IndyWallet wallet ) {
        super(name, pool, wallet);
    }


    public CompletableFuture<Map<String, Map.Entry<String, String>>> verifyProof( ProofRequest proofRequest, Proof proof ) {
        return getEntitiesFromLedger(proof.getIdentifiers()).thenCompose(wrapException(entitiesFromLedger -> Anoncreds.verifierVerifyProof(proofRequest.toJSON(), proof.toJSON(), JSONUtil.mapper.writeValueAsString(entitiesFromLedger.getSchemas()), JSONUtil.mapper.writeValueAsString(entitiesFromLedger.getClaimDefs()), "{}")))
                                                            .thenApply(result -> {
                                                                if ( !result ) {
                                                                    throw new IndyWrapperException("Invalid proof: verifierVerifyProof returned false");
                                                                }

                                                                boolean allEncodingsValid = proof.getRequestedProof()
                                                                                                 .getRevealedAttributes()
                                                                                                 .values()
                                                                                                 .stream()
                                                                                                 .allMatch(IntegerEncodingUtil::validateProofEncoding);

                                                                if ( !allEncodingsValid ) {
                                                                    throw new IndyWrapperException("Invalid proof: encodings invalid");
                                                                }


                                                                // Get both the self attested and the revealed attributes
                                                                Stream<Map.Entry<String, String>> selfAttestedStream = proof.getRequestedProof()
                                                                                                                            .getSelfAttestedAttributes()
                                                                                                                            .entrySet()
                                                                                                                            .stream();
                                                                Stream<Map.Entry<String, String>> revealedStream = proof.getRequestedProof()
                                                                                                                        .getRevealedAttributes()
                                                                                                                        .entrySet()
                                                                                                                        .stream()
                                                                                                                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()
                                                                                                                                                                                         .get(1)));

                                                                Map<String, Map.Entry<String, String>> attributeValues = Stream.concat(selfAttestedStream, revealedStream)
                                                                                                                               .collect(Collectors.toMap(Map.Entry::getKey, entry -> new AbstractMap.SimpleEntry<>(proofRequest.getRequestedAttrs()
                                                                                                                                                                                                                               .get(entry.getKey())
                                                                                                                                                                                                                               .getName(), entry.getValue())));

                                                                return attributeValues;
                                                            });

    }
}
