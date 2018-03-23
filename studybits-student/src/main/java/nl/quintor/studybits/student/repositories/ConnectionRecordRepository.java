package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.model.ConnectionRecord;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRecordRepository extends JpaRepository<ConnectionRecord, Long> {
    List<ConnectionRecord> findAllByStudent(Student student);
    List<ConnectionRecord> findAllByUniversity(University university);

    List<ConnectionRecord> findAllByStudent_Id(Long studentId);

    List<ConnectionRecord> findAllByUniversity_Name(String name);
}
