package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ClaimRequest implements Serializable, AuthCryptable {
    @JsonProperty("prover_did")
    private String proverDid;
    @JsonProperty("issuer_did")
    private String issuerDid;
    @JsonProperty("schema_key")
    private SchemaKey schemaKey;

    @JsonProperty("blinded_ms")
    private JsonNode blindedMs;

    @JsonProperty("blinded_ms_correctness_proof")
    private JsonNode blindedMsCorrectnessProof;

    private String nonce;

    @JsonIgnore
    private String myDid;
    @JsonIgnore
    private String theirDid;
}
