package nl.quintor.studybits.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDefinitionModel {
    private String name;
    private String version;
    private List<String> attrNames;
}
