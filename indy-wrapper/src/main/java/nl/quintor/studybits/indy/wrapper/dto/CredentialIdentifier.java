package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialIdentifier implements Serializable {
    @JsonProperty( "schema_id" )
    private String schemaId;
    @JsonProperty( "cred_def_id" )
    private String credDefId;
    @JsonProperty( "rev_reg_id" )
    private Integer revRegId;
    private Long timestamp;

    public CredentialIdentifier(CredentialReferent claimReferent ) {
        this(claimReferent.getCredentialInfo().getSchemaId(), claimReferent.getCredentialInfo().getCredDefId(), claimReferent.getCredentialInfo().getRevRegId(), null);
    }
}
