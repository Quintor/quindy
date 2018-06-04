package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.dto.TranscriptProof;
import nl.quintor.studybits.university.entities.*;
import nl.quintor.studybits.university.enums.ExchangeApplicationState;
import nl.quintor.studybits.university.models.ExchangeApplicationModel;
import nl.quintor.studybits.university.repositories.ExchangeApplicationRepository;
import nl.quintor.studybits.university.repositories.ExchangePositionRepository;
import nl.quintor.studybits.university.repositories.TranscriptProofRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeApplicationService {

    private final ExchangeApplicationRepository exchangeApplicationRepository;
    private final TranscriptProofRepository transcriptProofRepository;
    private final ExchangePositionRepository exchangePositionRepository;
    private final UserService userService;
    private final UniversityService universityService;
    private final Mapper mapper;

    public void create(User prover, ProofRecord proofRecord, TranscriptProof proof) {
        University university = proofRecord.getExchangePositionRecord().getUniversity();

        TranscriptProofRecord transcriptProofRecord = mapper.map(proof, TranscriptProofRecord.class);
        transcriptProofRepository.saveAndFlush(transcriptProofRecord);

        ExchangeApplicationRecord applicationRecord = new ExchangeApplicationRecord(null, university, prover, proofRecord.getExchangePositionRecord(), ExchangeApplicationState.APPLIED, transcriptProofRecord);
        exchangeApplicationRepository.saveAndFlush(applicationRecord);
    }

    public List<ExchangeApplicationRecord> findAllForUniversity(String universityName) {
        return exchangeApplicationRepository
                .findAllByUniversityNameIgnoreCase(universityName);
    }

    public void updateState(ExchangeApplicationModel model) {
        ExchangeApplicationRecord record = getByUniversityAndUserAndExchangePosition(model);
        record.setState(model.getState());

        exchangeApplicationRepository.save(record);
    }

    private ExchangeApplicationRecord getByUniversityAndUserAndExchangePosition(ExchangeApplicationModel model) {
        University university = universityService.getUniversity(model.getUniversityName());
        User user = userService.getByUniversityNameAndUserName(model.getUniversityName(), model.getUserName());
        ExchangePositionRecord position = exchangePositionRepository
                .findByProofRecordId(model.getExchangePositionModel().getProofRecordId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find Exchange Position with ProofRecordId: {}", model.getExchangePositionModel().getProofRecordId())));

        return exchangeApplicationRepository
                .findByUniversityAndUserAndExchangePositionRecord(university, user, position)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find ExchangeApplicationRecord for Model: {}", model)));
    }

    public List<ExchangeApplicationRecord> findAllByUserName(String userName) {
        return exchangeApplicationRepository.findAllByUserUserName(userName);
    }
}
