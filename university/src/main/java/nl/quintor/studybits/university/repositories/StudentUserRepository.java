package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.StudentUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentUserRepository extends JpaRepository<StudentUser, Long> {}
