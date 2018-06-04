package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.ExchangeApplicationRecord;
import nl.quintor.studybits.student.entities.TranscriptProofRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranscriptProofRepository extends JpaRepository<TranscriptProofRecord, Long> {
    Optional<TranscriptProofRecord> findByExchangeApplicationRecord(ExchangeApplicationRecord record);
}
