package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Filter implements Serializable {
    @JsonProperty( "issuer_did" )
    private String issuerDid;
    @JsonProperty( "schema_key" )
    private SchemaKey schemaKey;
}
