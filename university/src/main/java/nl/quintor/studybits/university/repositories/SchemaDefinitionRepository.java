package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.SchemaDefinitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemaDefinitionRepository extends JpaRepository<SchemaDefinitionRecord, Long> {
}