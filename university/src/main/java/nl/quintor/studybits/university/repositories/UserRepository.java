package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(String universityName, String userName);

    List<User> findAllByUniversityNameIgnoreCase(String universityName);

    @Query("select u.id from User u where upper(u.userName) = upper(:userName) and upper(u.university.name) = upper(:universityName)")
    Optional<Long> findIdByUniversityNameAndUserName(@Param("universityName") String universityName, @Param("userName") String userName);

}