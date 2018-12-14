package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialOffer implements Serializable {
    @JsonProperty("issuer_did")
    private String issuerDid;
    @JsonProperty("schema_id")
    private String schemaId;
    @JsonProperty("cred_def_id")
    private String credDefId;
    @JsonProperty("key_correctness_proof")
    private JsonNode keyCorrectnessProof;

    private String nonce;
}
