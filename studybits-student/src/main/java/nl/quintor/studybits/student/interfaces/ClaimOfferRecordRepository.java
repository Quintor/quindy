package nl.quintor.studybits.student.interfaces;

import nl.quintor.studybits.student.model.ClaimOfferRecord;
import nl.quintor.studybits.student.model.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClaimOfferRecordRepository extends CrudRepository<ClaimOfferRecord, Long> {
    List<ClaimOfferRecord> findAllByOwner(@Param("owner") Student owner);

    ClaimOfferRecord getById(@Param("Id") Long id);
}
