package nl.quintor.studybits.repositories;

import nl.quintor.studybits.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>{
    Optional<Student> findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(String universityName, String userName);

    List<Student> findAllByUniversityNameIgnoreCase(String universityName);
}