package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRecordRepository extends JpaRepository<ClaimRecord, Long> {
    List<ClaimRecord> findAllByOwner(Student owner);
}
