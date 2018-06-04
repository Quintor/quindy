package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.entities.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    boolean existsByNameIgnoreCase(String name );
    Optional<University> findByNameIgnoreCase(String name );
}