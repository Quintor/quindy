package nl.quintor.studybits.university.services;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.dto.Transcript;
import nl.quintor.studybits.university.dto.TranscriptProof;
import nl.quintor.studybits.university.entities.ProofRecord;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TranscriptProofService extends ProofHandler<TranscriptProof> {

    @Autowired
    public TranscriptProofService(UniversityService universityService, ProofRecordRepository proofRecordRepository, ClaimSchemaRepository claimSchemaRepository, ExchangeApplicationService exchangeApplicationService, UserRepository userRepository, Mapper mapper) {
        super(universityService, proofRecordRepository, claimSchemaRepository, exchangeApplicationService, userRepository, mapper);
    }

    @Override
    protected Class<TranscriptProof> getProofType() {
        return TranscriptProof.class;
    }

    @Override
    protected boolean handleProof(Object object, ProofRecord proofRecord, TranscriptProof proof) {
        Transcript transcript = (Transcript) object;

        Validate.isTrue(transcript.getDegree().equalsIgnoreCase(proof.getDegree()), "Degree mismatch.");
        Validate.isTrue(transcript.getStatus().equalsIgnoreCase(proof.getStatus()), "Status mismatch.");

        if (proofRecord.getExchangePositionRecord() != null) {
            exchangeApplicationService.create(transcript, proofRecord, proof);
        }

        return true;
    }

}