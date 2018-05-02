package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.SchemaDefinitionRecord;
import nl.quintor.studybits.university.repositories.SchemaDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SchemaDefinitionService {

    private final SchemaDefinitionRepository schemaDefinitionRepository;

    public SchemaDefinitionRecord getByNameAndVersion(String name, String version) {
        return schemaDefinitionRepository
                .findByNameIgnoreCaseAndVersion(name, version)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find SchemaDefinition with name: %s and version %s", name, version)));
    }


}
