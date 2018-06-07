package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaKey {
    private String name;
    private String version;
    private String did;

    public static SchemaKey fromSchema(SchemaDefinition schemaDefinition, String did) {
        return new SchemaKey(schemaDefinition.getName(), schemaDefinition.getVersion(), did);
    }

}
