package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.AdminUser;
import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(String universityName, String userName);

    Optional<User> findByFirstNameAndLastNameAndSsn(String firstName, String lastName, String ssn);

    List<User> findAllByUniversityNameIgnoreCase(String universityName);

    Optional<User> findByStudentUserIsNotNullAndId(Long id);

    Optional<User> findByAdminUserIsNotNullAndId(Long id);

    List<User> findAllByStudentUserIsNotNull();

    List<User> findAllByAdminUserIsNotNull();

    List<User> findAllByStudentUserIsNotNullAndUniversityNameIgnoreCase(String universityName);

    Optional<User> findAllByStudentUserIsNotNullAndUniversityNameIgnoreCaseAndUserNameIgnoreCase(String universityName, String userName);

    List<User> findAllByAdminUserIsNotNullAndUniversityNameIgnoreCase(String universityName);

    @Query("select u.id from User u where upper(u.userName) = upper(:userName) and upper(u.university.name) = upper(:universityName)")
    Long findIdByUniversityNameAndUserName(@Param("universityName") String universityName, @Param("userName") String userName);


    default User saveStudentUser(StudentUser studentUser) {
        return save(studentUser.getUser());
    }

    default User saveAdminUser(AdminUser adminUser) {
        return save(adminUser.getUser());
    }
}