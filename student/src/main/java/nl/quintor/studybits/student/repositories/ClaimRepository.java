package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.model.Claim;
import nl.quintor.studybits.student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findAllByOwner(Student owner);
}
