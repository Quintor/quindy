package nl.quintor.studybits.student.interfaces;

import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.model.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClaimOfferRecordRepository extends CrudRepository<ClaimRecord, Long> {
    List<ClaimRecord> findAllByOwner(@Param("owner") Student owner);

    ClaimRecord getById(@Param("Id") Long id);
}
