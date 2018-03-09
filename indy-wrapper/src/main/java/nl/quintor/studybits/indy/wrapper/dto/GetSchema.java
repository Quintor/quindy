package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetSchema implements Serializable {
    private String name;
    private String version;

    public static GetSchema fromSchemaKey(SchemaKey schemaKey) {
        return new GetSchema(schemaKey.getName(), schemaKey.getVersion());
    }
}
