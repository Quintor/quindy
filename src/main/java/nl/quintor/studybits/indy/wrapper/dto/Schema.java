package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schema implements Serializable {
    private String identifier;
    private String type;
    private long txnTime;
    @JsonProperty("state_proof")
    private JsonNode stateProof;
    private long reqId;
    private SchemaDefinition data;
    private int seqNo;
    private String dest;
}
