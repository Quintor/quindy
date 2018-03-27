package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimInfo {
    @JsonProperty( "schema_key" )
    private SchemaKey schemaKey;

    @JsonProperty( "issuer_did" )
    private String issuer_did;

    private String referent;

    @JsonProperty( "attrs" )
    private Map<String, Object> attrs;

    @JsonProperty( "revoc_reg_seq_no" )
    private Integer revReqSeqNo;
}