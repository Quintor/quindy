package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.SchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemaKeyRepository extends JpaRepository<SchemaKey, Long> {
    Optional<SchemaKey> findByNameAndVersion(String name, String version);
    List<SchemaKey> findByName(String name);
}
