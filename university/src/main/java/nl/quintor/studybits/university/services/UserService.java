package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.AdminUser;
import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.UniversityRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class UserService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final UserProofService userProofService;
    private final Mapper mapper;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllForUniversity(String universityName) {
        return userRepository
                .findAllByUniversityNameIgnoreCase(universityName);
    }

    public User getById(Long userId) {
        return userRepository.getOne(userId);
    }

    public Optional<User> findByUniversityAndUserName(String universityName, String userName) {
        return userRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName);
    }

    public User createStudent(String universityName, String userName, String firstName, String lastName, String ssn, boolean confirmed) {
        University university = getUniversity(universityName);
        User user = new User(userName, firstName, lastName, ssn, confirmed, university, new StudentUser());
        return save(user, confirmed);
    }

    public User createAdmin(String universityName, String userName, String firstName, String lastName, String ssn, boolean confirmed) {
        University university = getUniversity(universityName);
        User user = new User(userName, firstName, lastName, ssn, confirmed, university, new AdminUser());
        return save(user, confirmed);
    }

    private User save(User user, boolean confirmed) {
        User result = userRepository.save(user);
        if (!confirmed) {
            userProofService.addProofRequest(result.getId());
        }

        return result;
    }

    private University getUniversity(String universityName) {
        return universityRepository.findByNameIgnoreCase(universityName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown university."));
    }

}