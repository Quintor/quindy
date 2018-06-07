package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaDefinition implements Serializable {
    private String name;
    private String version;
    @JsonProperty("attr_names")
    private List<String> attrNames;
}
