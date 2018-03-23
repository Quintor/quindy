package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ClaimReferent implements Serializable {
    private String referent;
    private Map<String, String> attrs;
    @JsonProperty("schema_key")
    private SchemaKey schemaKey;
    @JsonProperty("issuer_did")
    private String issuerDid;
    @JsonProperty("revoc_reg_seq_no")
    private Integer revocRegSeqNo;
}
