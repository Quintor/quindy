package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.university.entities.ClaimSchema;
import nl.quintor.studybits.university.entities.ExchangePositionRecord;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.models.ExchangePositionModel;
import nl.quintor.studybits.university.repositories.ExchangePositionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangePositionService {

    private final UniversityService universityService;
    private final ClaimService claimService;
    private final ExchangePositionRecordRepository exchangePositionRecordRepository;

    public ExchangePositionRecord create(ExchangePositionModel model) {
        University university = universityService.getUniversity(model.getUniversityName());
        ClaimSchema claimSchema = claimService.getClaimSchemaByNameAndVersion(university.getName(), model.getSchemaName(), model.getSchemaVersion());
        ExchangePositionRecord record = new ExchangePositionRecord(null, university, claimSchema, model.getIsOpen(), model.getAttributes());
        return exchangePositionRecordRepository.save(record);
    }

    public List<ExchangePositionRecord> findAll() {
        return exchangePositionRecordRepository.findAll();
    }

    public List<ExchangePositionRecord> findAllByUniversityName(String universityName) {
        return exchangePositionRecordRepository.findAllByUniversityName(universityName);
    }

    public ProofRequest createProofRequestForExchangePosition(ExchangePositionRecord record) {
        // TODO: todo todo todo todoooooo todododo
        return null;
    }
}
