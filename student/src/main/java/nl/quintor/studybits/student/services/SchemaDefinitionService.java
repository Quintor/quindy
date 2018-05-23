package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.SchemaDefinitionRecord;
import nl.quintor.studybits.student.models.SchemaDefinitionModel;
import nl.quintor.studybits.student.repositories.SchemaDefinitionRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SchemaDefinitionService {

    private final SchemaDefinitionRepository schemaDefinitionRepository;
    private final Mapper mapper;

    @Transactional
    public SchemaDefinitionRecord fromModel(SchemaDefinitionModel model) {
        SchemaDefinitionRecord record = mapper.map(model, SchemaDefinitionRecord.class);
        return schemaDefinitionRepository
                .findByNameIgnoreCaseAndVersion(record.getName(), record.getVersion())
                .orElseGet(() -> schemaDefinitionRepository.save(record));
    }
}
