package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.entities.ClaimSchema;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimService {

    private final ClaimRecordRepository claimRecordRepository;
    private final ClaimSchemaRepository claimSchemaRepository;

    public List<ClaimRecord> findAvailableClaims(Long userId) {
        return claimRecordRepository.findAllByUserId(userId);
    }

    public ClaimSchema getClaimSchemaByNameAndVersion(String universityName, String schemaName, String schemaVersion) {
        return claimSchemaRepository
                .findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(universityName, schemaName, schemaVersion)
                .orElseThrow(() -> new IllegalArgumentException("ClaimSchema with name and version not found."));
    }
}
