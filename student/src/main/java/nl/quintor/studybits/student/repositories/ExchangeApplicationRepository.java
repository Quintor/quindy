package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.ExchangeApplicationRecord;
import nl.quintor.studybits.student.entities.ExchangePositionRecord;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeApplicationRepository extends JpaRepository<ExchangeApplicationRecord, Long> {
    List<ExchangeApplicationRecord> findAllByStudentUserName(String userName);
    Optional<ExchangeApplicationRecord> findByUniversityAndStudentAndExchangePositionRecord(University university, Student student, ExchangePositionRecord exchangePositionRecord);
    boolean existsByUniversityAndStudentAndExchangePositionRecord(University university, Student student, ExchangePositionRecord exchangePositionRecord);
}