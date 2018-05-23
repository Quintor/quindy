package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.TranscriptProofRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranscriptProofRepository extends JpaRepository<TranscriptProofRecord, Long> {
}