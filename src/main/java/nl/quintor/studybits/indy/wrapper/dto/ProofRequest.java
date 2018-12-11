package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProofRequest implements Serializable {
    private String nonce;
    private String name;
    private String version;

    @Singular
    @JsonProperty( "requested_attributes" )
    private Map<String, AttributeInfo> requestedAttributes;

    @Singular
    @JsonProperty( "requested_predicates" )
    private Map<String, PredicateInfo> requestedPredicates;
}
