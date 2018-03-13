package nl.quintor.studybits.student.interfaces;

import nl.quintor.studybits.student.model.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends CrudRepository<Student, Long> {
    Student getByUsername(@Param("username") String username);

    Student getById(@Param("Id") Long id);
}
