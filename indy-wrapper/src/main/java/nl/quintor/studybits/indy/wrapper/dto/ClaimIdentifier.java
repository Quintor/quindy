package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ClaimIdentifier implements Serializable {
    @JsonProperty( "schema_key" )
    private SchemaKey schemaKey;
    @JsonProperty( "issuer_did" )
    private String issuerDid;
    @JsonProperty( "rev_reg_seq_no" )
    private Integer revRegSeqNo;

    public ClaimIdentifier( ClaimReferent claimReferent ) {
        this(claimReferent.getSchemaKey(), claimReferent.getIssuerDid(), claimReferent.getRevocRegSeqNo());
    }
}
