package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.SchemaDefinitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemaDefinitionRepository extends JpaRepository<SchemaDefinitionRecord, Long> {
    Optional<SchemaDefinitionRecord> findByNameIgnoreCaseAndVersion(String name, String version);
}