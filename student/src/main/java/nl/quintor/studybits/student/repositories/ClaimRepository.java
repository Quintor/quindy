package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.ClaimEntity;
import nl.quintor.studybits.student.entities.SchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<ClaimEntity, Long> {
    List<ClaimEntity> findAllByStudentId(Long studentId);
    List<ClaimEntity> findAllBySchemaId(String schemaId);
    boolean existsBySchemaIdAndLabel(String schemaId, String label);
}
