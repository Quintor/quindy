package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.StudentClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentClaimRepository extends JpaRepository<StudentClaim, Long> {

}