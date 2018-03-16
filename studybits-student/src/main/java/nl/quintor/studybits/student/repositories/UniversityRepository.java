package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.model.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {
    boolean existsByName(String name);

    Optional<University> findByName(String name);
}
