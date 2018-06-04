package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.ProofRequestRecord;
import nl.quintor.studybits.student.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProofRequestRecordRepository extends JpaRepository<ProofRequestRecord, Long> {
    boolean existsByStudentAndNameAndVersion(Student student, String name, String version);
    List<ProofRequestRecord> findAllByStudentUserNameIgnoreCase(String studentUserName);
    Optional<ProofRequestRecord> findByStudentUserNameIgnoreCaseAndNameIgnoreCaseAndVersion(String studentUserName, String name, String version);
}
