package nl.quintor.studybits.university.services;


import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.dto.ClaimUtils;
import nl.quintor.studybits.university.dto.Proof;
import nl.quintor.studybits.university.dto.Version;
import nl.quintor.studybits.university.entities.ProofRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class ProofService {

    private final ProofRecordRepository proofRecordRepository;
    private final UserService userService;

    public List<ProofRecord> findProofRequests(Long userId ) {
        return proofRecordRepository.findAllByUserId(userId);
    }


    public <T extends Proof> ProofRecord addProofRequest(Long userId, Class<T> proofType) {
        User user = userService.getById(userId);
        Version version = ClaimUtils.getVersion(proofType);
        String nonce = RandomStringUtils.randomAlphanumeric(28, 36);
        ProofRecord proofRecord = new ProofRecord(null, user, version.getName(), version.getVersion(), nonce, null);
        return proofRecordRepository.save(proofRecord);
    }

}