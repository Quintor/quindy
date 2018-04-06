package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.ProofRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProofRecordRepository extends JpaRepository<ProofRecord, Long> {
    List<ProofRecord> findAllByUserId(Long userId);

    List<ProofRecord> findAllByUserIdAndProofMessageIsNull(Long userId);
}