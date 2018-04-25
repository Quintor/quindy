package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.ExchangePositionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangePositionRecordRepository extends JpaRepository<ExchangePositionRecord, Long> {
    List<ExchangePositionRecord> findAllByUniversityName(String universityName);
}