package nl.quintor.studybits.student.interfaces;

import nl.quintor.studybits.student.model.University;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UniversityRepository extends CrudRepository<University, Long> {
    University getByName(@Param("name") String name);
}
