package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CredentialInfo {
    @JsonProperty( "schema_id" )
    private String schemaId;

    @JsonProperty( "cred_def_id" )
    private String credDefId;

    private String referent;

    @JsonProperty( "attrs" )
    private Map<String, String> attrs;

    @JsonProperty( "rev_reg_id" )
    private Integer revRegId;

    @JsonProperty("cred_rev_id")
    private Integer credRevId;
}