package nl.quintor.studybits.university.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ProofAttribute {
    private final Field field;
    private final String attributeName;

    private final List<SchemaVersion> schemaVersions;

    public String getFieldName() {
        return field.getName();
    }

    public ProofAttribute(Field field) {
        this(field, field.getName(), new ArrayList<>());
    }
}