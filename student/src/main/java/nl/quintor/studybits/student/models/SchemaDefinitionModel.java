package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDefinitionModel {
    private Long id;
    private String name;
    private String version;
    private List<String> attrNames;
}
