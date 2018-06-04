package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.ExchangeApplicationRecord;
import nl.quintor.studybits.university.entities.ExchangePositionRecord;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeApplicationRepository extends JpaRepository<ExchangeApplicationRecord, Long> {
    List<ExchangeApplicationRecord> findAllByUniversityNameIgnoreCase(String universityName);

    List<ExchangeApplicationRecord> findAllByUserUserName(String userName);

    Optional<ExchangeApplicationRecord> findByUniversityAndUserAndExchangePositionRecord(University university, User user, ExchangePositionRecord exchangePositionRecord);
}