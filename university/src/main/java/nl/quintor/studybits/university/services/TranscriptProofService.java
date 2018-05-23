package nl.quintor.studybits.university.services;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.dto.TranscriptProof;
import nl.quintor.studybits.university.entities.ProofRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class TranscriptProofService extends ProofHandler<TranscriptProof> {

    private final ExchangeApplicationService exchangeApplicationService;

    @Autowired
    public TranscriptProofService(UniversityService universityService, ProofRecordRepository proofRecordRepository, ClaimSchemaRepository claimSchemaRepository, ExchangeApplicationService exchangeApplicationService, UserRepository userRepository, Mapper mapper) {
        super(universityService, proofRecordRepository, claimSchemaRepository, userRepository, mapper);
        this.exchangeApplicationService = exchangeApplicationService;
    }

    @Override
    protected Class<TranscriptProof> getProofType() {
        return TranscriptProof.class;
    }

    @Override
    protected boolean handleProof(User prover, ProofRecord proofRecord, TranscriptProof proof) {
        Map<String, String> attributes = proofRecord.getExchangePositionRecord().getAttributes();

        Validate.isTrue(attributes.get("degree").equalsIgnoreCase(proof.getDegree()), "Degree mismatch.");
        Validate.isTrue(attributes.get("status").equalsIgnoreCase(proof.getStatus()), "Status mismatch.");

        if (proofRecord.getExchangePositionRecord() != null) {
            exchangeApplicationService.create(prover, proofRecord, proof);
        }

        return true;
    }

}