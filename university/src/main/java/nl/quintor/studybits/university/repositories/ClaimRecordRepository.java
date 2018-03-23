package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.ClaimRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRecordRepository extends JpaRepository<ClaimRecord, Long> {
    List<ClaimRecord> findAllByUserId( Long userId );
}