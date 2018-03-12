package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ClaimOffer implements Serializable, AuthCryptable {
    @JsonProperty("issuer_did")
    private String issuerDid;
    @JsonProperty("schema_key")
    private SchemaKey schemaKey;
    @JsonProperty("key_correctness_proof")
    private JsonNode keyCorrectnessProof;

    private String nonce;

    @JsonIgnore
    @Setter
    private String theirDid;
}
