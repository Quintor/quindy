package nl.quintor.studybits.university.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaDefinitionRecord {
    private String name;
    private String version;
    @JsonProperty("attr_names")
    private List<String> attrNames;
}
