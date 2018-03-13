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
public class Claim implements Serializable, AuthCryptable {
    private JsonNode values;
    @JsonProperty("schema_key")
    private SchemaKey schemaKey;
    private JsonNode signature;
    @JsonProperty("signature_correctness_proof")
    private JsonNode signatureCorrectnessProof;
    @JsonProperty("issuer_did")
    private String issuerDid;
    @JsonProperty("rev_reg_seq_no")
    private Integer revReqSeqNo;

    @JsonIgnore
    private String myDid;
    @JsonIgnore
    private String theirDid;
}
