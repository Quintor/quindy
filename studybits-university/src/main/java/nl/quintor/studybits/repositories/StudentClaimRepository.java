package nl.quintor.studybits.repositories;

import nl.quintor.studybits.entities.StudentClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentClaimRepository extends JpaRepository<StudentClaim, Long> {

}