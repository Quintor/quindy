package nl.quintor.studybits.university.services;


import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.ProofRecord;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class ProofService {

    private final ProofRecordRepository proofRecordRepository;

    public List<ProofRecord> findAllProofRequestRecords(Long userId ) {
        return proofRecordRepository.findAllByUserIdAndProofMessageIsNull(userId);
    }

}