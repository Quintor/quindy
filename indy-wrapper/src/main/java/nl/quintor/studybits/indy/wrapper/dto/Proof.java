package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Proof implements Serializable, AuthCryptable {
    private JsonNode proof;
    @JsonProperty("requested_proof")
    private JsonNode requestedProof;
    private JsonNode identifiers;

    @JsonIgnore
    private String theirDid;
}
