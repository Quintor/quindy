package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.model.SchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemaKeyRepository extends JpaRepository<SchemaKey, Long> {
    Optional<SchemaKey> findByName(String name);
}
