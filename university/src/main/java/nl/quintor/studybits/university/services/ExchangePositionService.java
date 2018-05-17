package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.university.controllers.student.ProofRequestController;
import nl.quintor.studybits.university.entities.ExchangePositionRecord;
import nl.quintor.studybits.university.entities.ProofRecord;
import nl.quintor.studybits.university.entities.SchemaDefinitionRecord;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.models.ExchangePositionModel;
import nl.quintor.studybits.university.repositories.ExchangePositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangePositionService {

    private final UniversityService universityService;
    private final ExchangePositionRepository exchangePositionRepository;
    private final SchemaDefinitionService schemaDefinitionService;
    private final ProofService proofService;

    @Transactional
    public ExchangePositionRecord create(ExchangePositionModel model) {
        University university = universityService.getUniversity(model.getUniversityName());
        SchemaDefinitionRecord schemaDefinitionRecord = schemaDefinitionService.getByNameAndVersion(model.getSchemaDefinitionRecord().getName(), model.getSchemaDefinitionRecord().getVersion());

        ProofHandler handler = proofService.getHandler(schemaDefinitionRecord.getName().toLowerCase() + "proof");
        ProofRecord proofRecord = handler.addProofRequest(university.getUser().getId());

        ExchangePositionRecord record = new ExchangePositionRecord(null, university, schemaDefinitionRecord, proofRecord, model.getState(), model.getAttributes());

        return exchangePositionRepository.save(record);
    }

    public ExchangePositionRecord getByProofRecordId(Long proofRecordId) {
        return exchangePositionRepository
                .findByProofRecordId(proofRecordId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find ExchangePositionRecord by ProofRecordId: %d", proofRecordId)));
    }

    public List<ExchangePositionRecord> findAll() {
        return exchangePositionRepository.findAll();
    }

    public List<ExchangePositionRecord> findAllByUniversityName(String universityName) {
        return exchangePositionRepository.findAllByUniversityName(universityName);
    }

    public ProofRequest createProofRequestForExchangePosition(ExchangePositionRecord record) {
        // TODO: todo todo todo todoooooo todododo
        return null;
    }
}
