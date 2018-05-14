package nl.quintor.studybits.university.services;


import lombok.Getter;
import nl.quintor.studybits.university.entities.ProofRecord;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProofService {

    private final ProofRecordRepository proofRecordRepository;
    @Getter
    private final Map<String, ProofHandler> proofHandlers;

    @Autowired
    public ProofService(ProofRecordRepository proofRecordRepository, ProofHandler[] proofHandlers) {
        this.proofRecordRepository = proofRecordRepository;
        this.proofHandlers = Arrays.stream(proofHandlers)
                .collect(Collectors.toMap(x -> x.getProofName().toLowerCase(), x -> x));
    }

    public List<ProofRecord> findAllProofRequestRecords(Long userId ) {
        return proofRecordRepository.findAllByUserIdAndProofJsonIsNull(userId);
    }

    public ProofHandler getHandler(String proofName) {
        Validate.notNull(proofName, "Proof name cannot be null.");
        return Validate.notNull(proofHandlers.get(proofName.toLowerCase()), String.format("Unknown proof: %s", proofName));
    }
}