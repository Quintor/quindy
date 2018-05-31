package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.ConnectionRecord;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRecordRepository extends JpaRepository<ConnectionRecord, Long> {
    List<ConnectionRecord> findAllByStudentUserNameIgnoreCase(String userName);
    Optional<ConnectionRecord> findByStudentAndUniversity(Student student, University university);
}
