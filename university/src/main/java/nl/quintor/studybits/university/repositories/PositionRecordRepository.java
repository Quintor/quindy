package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.PositionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRecordRepository extends JpaRepository<PositionRecord, Long> {
}