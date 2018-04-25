package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.ExchangePositionRecord;
import nl.quintor.studybits.student.entities.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangePositionRecordRepository extends JpaRepository<ExchangePositionRecord, Long> {
    Boolean existsByUniversitySeqNoAndUniversityName(Long universitySeqNo, String universityName);
    List<ExchangePositionRecord> findAllByUniversity(University university);
}