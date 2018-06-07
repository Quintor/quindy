package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialDefinition implements Serializable {
    private String ver;
    private String id;
    private String schemaId;
    private String type;
    private String tag;
    private JsonNode value;
}
