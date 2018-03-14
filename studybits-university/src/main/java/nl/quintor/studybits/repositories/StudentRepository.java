package nl.quintor.studybits.repositories;

import nl.quintor.studybits.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>{
    Optional<Student> findByUserName(String userName);
}
