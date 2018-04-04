package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.SchemaKey;
import nl.quintor.studybits.student.repositories.SchemaKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SchemaKeyService {
    private SchemaKeyRepository schemaKeyRepository;

    public SchemaKey findByNameOrElseThrow(String name) {
        return schemaKeyRepository
                .findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("SchemaKey with name not found."));
    }
}
