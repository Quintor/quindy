package nl.quintor.studybits.student.interfaces;

import nl.quintor.studybits.student.model.Claim;
import nl.quintor.studybits.student.model.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClaimRepository extends CrudRepository<Claim, Long> {
    List<Claim> findAllByOwner(@Param("owner") Student owner);

    Claim getById(@Param("Id") Long id);

}
