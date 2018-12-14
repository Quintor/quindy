package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Credential implements Serializable {
    private JsonNode values;
    @JsonProperty("schema_id")
    private String schemaId;
    @JsonProperty("cred_def_id")
    private String credDefId;
    private JsonNode signature;
    @JsonProperty("signature_correctness_proof")
    private JsonNode signatureCorrectnessProof;
    @JsonProperty("rev_reg_id")
    private Integer revRegId;
    @JsonProperty("rev_reg")
    private JsonNode revReg;
    private JsonNode witness;
}
