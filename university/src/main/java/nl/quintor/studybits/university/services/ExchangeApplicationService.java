package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.dto.Transcript;
import nl.quintor.studybits.university.dto.TranscriptProof;
import nl.quintor.studybits.university.entities.*;
import nl.quintor.studybits.university.enums.ExchangeApplicationState;
import nl.quintor.studybits.university.models.ExchangeApplicationModel;
import nl.quintor.studybits.university.models.ExchangePositionModel;
import nl.quintor.studybits.university.repositories.ExchangeApplicationRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeApplicationService {

    private final ExchangeApplicationRepository exchangeApplicationRepository;
    private final ExchangePositionService exchangePositionService;
    private final UserService userService;
    private final UniversityService universityService;
    private final Mapper mapper;

    public void create(Transcript transcript, ProofRecord proofRecord, TranscriptProof proof) {
        University university = proofRecord.getExchangePositionRecord().getUniversity();
        User user = userService.getByFirstNameAndLastNameAndSsn(transcript.getFirstName(), transcript.getLastName(), transcript.getSsn());
        ExchangeApplicationRecord applicationRecord = new ExchangeApplicationRecord(null, university, user, proofRecord.getExchangePositionRecord(), ExchangeApplicationState.APPLIED, proof);
        exchangeApplicationRepository.saveAndFlush(applicationRecord);
    }

    public List<ExchangeApplicationRecord> findAllForUniversity(String universityName) {
        return exchangeApplicationRepository
                .findAllByUniversityNameIgnoreCase(universityName);
    }

    public void update(ExchangeApplicationModel model) {
        ExchangeApplicationRecord record = fromModel(model);
        exchangeApplicationRepository.save(record);
    }

    public ExchangeApplicationModel toModel(ExchangeApplicationRecord record) {
        ExchangeApplicationModel model = mapper.map(record, ExchangeApplicationModel.class);
        model.setExchangePositionModel(mapper.map(record.getExchangePositionRecord(), ExchangePositionModel.class));
        return model;
    }

    private ExchangeApplicationRecord fromModel(ExchangeApplicationModel model) {
        University university = universityService.getUniversity(model.getUniversityName());
        User user = userService.getByUniversityNameAndUserName(university.getName(), model.getUserName());
        ExchangePositionRecord exchangePositionRecord = exchangePositionService.getByProofRecordId(model.getExchangePositionModel().getProofRecordId());
        return new ExchangeApplicationRecord(model.getId(), university, user, exchangePositionRecord, model.getState(), model.getProof());
    }

    public List<ExchangeApplicationRecord> findAllByUserName(String userName) {
        return exchangeApplicationRepository.findAllByUserUserName(userName);
    }
}
