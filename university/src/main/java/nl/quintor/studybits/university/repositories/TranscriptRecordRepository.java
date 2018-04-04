package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.TranscriptRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranscriptRecordRepository extends JpaRepository<TranscriptRecord, Long> {
    List<TranscriptRecord> findAllByStudentUser(StudentUser studentUser);
}