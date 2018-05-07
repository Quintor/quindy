package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.ExchangePositionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangePositionRepository extends JpaRepository<ExchangePositionRecord, Long> {
    List<ExchangePositionRecord> findAllByUniversityName(String universityName);
    Optional<ExchangePositionRecord> findByProofRecordId(Long proofRecordId);
}