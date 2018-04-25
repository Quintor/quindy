package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.entities.ClaimSchema;
import nl.quintor.studybits.student.repositories.ClaimSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimSchemaService {

    private ClaimSchemaRepository claimSchemaRepository;

    public ClaimSchema getByUniversityNameAndSchemaNameAndSchemaVersion(String universityName, String schemaName, String schemaVersion) {
        return claimSchemaRepository
                .findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(universityName, schemaName, schemaVersion)
                .orElseThrow(() -> new IllegalArgumentException("ClaimSchema with uniName, schemaName, and schemaVersion not found"));
    }


}
