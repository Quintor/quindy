package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.Claim;
import nl.quintor.studybits.student.entities.SchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findAllByOwnerId(Long ownerId);

    boolean existsByHashId(String hashId);

    List<Claim> findAllBySchemaKey(SchemaKey schemaKey);

    boolean existsBySchemaKeyNameAndSchemaKeyVersionAndLabel(String schemaKeyName, String schemaKeyVersion, String label);
}
