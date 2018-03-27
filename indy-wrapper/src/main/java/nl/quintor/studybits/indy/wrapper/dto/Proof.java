package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Proof implements Serializable, AuthCryptable {
    private JsonNode proof;
    @JsonProperty( "requested_proof" )
    private RequestedProof requestedProof;
    private Map<String, ClaimIdentifier> identifiers;

    @JsonIgnore
    private String theirDid;

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class RequestedProof {
        private Map<String, String> predicates;
        @JsonProperty( "self_attested_attrs" )
        private Map<String, String> selfAttestedAttributes;
        @JsonProperty( "revealed_attrs" )
        private Map<String, List<String>> revealedAttributes;

        @JsonProperty( "unrevealed_attrs" )
        private JsonNode unrevealedAttrs;
    }
}
