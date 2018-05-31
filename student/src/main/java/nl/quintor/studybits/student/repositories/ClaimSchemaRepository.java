package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.ClaimSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimSchemaRepository extends JpaRepository<ClaimSchema, Long> {
    Optional<ClaimSchema> findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(String universityName, String schemaName, String schemaVersion);
    Optional<ClaimSchema> findByUniversityIdAndSchemaNameAndSchemaVersion(Long universityId, String schemaName, String schemaVersion);
}