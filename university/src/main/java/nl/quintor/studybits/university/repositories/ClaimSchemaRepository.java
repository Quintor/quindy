package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.ClaimSchema;
import nl.quintor.studybits.university.entities.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimSchemaRepository extends JpaRepository<ClaimSchema, Long> {
    Optional<ClaimSchema> findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(String universityName, String schemaName, String schemaVersion);
}