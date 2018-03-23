package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProofRequest implements Serializable, AuthCryptable {
    private String nonce;
    private String name;
    private String version;

    @Singular
    @JsonProperty( "requested_attrs" )
    private Map<String, AttributeInfo> requestedAttrs;

    @Singular
    @JsonProperty( "requested_predicates" )
    private Map<String, PredicateInfo> requestedPredicates;

    @JsonIgnore
    private String theirDid;
}
