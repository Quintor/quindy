package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.SchemaKey;
import nl.quintor.studybits.student.repositories.SchemaKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SchemaKeyService {
    private SchemaKeyRepository schemaKeyRepository;

    public List<SchemaKey> getAllByName(String name) {
        return schemaKeyRepository
                .findByNameIgnoreCase(name);
    }

    @Transactional
    public SchemaKey getOrCreate(SchemaKey schemaKey) {
        return schemaKeyRepository
                .findByNameIgnoreCaseAndVersion(schemaKey.getName(), schemaKey.getVersion())
                .orElseGet(() -> schemaKeyRepository.save(schemaKey));
    }
}
