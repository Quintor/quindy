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
    private final Mapper mapper;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllForUniversity(String universityName) {
        return userRepository
                .findAllByUniversityNameIgnoreCase(universityName);
    }

    public Optional<User> findByUniversityAndUserName(String universityName, String userName) {
        return userRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName);
    }

    public User createStudent(String universityName, String userName, String firstName, String lastName, String ssn) {
        University university = getUniversity(universityName);
        User user = new User(userName, firstName, lastName, ssn, university, new StudentUser());
        return userRepository.save(user);
    }

    public User createAdmin(String universityName, String userName, String firstName, String lastName, String ssn) {
        University university = getUniversity(universityName);
        User user = new User(userName, firstName, lastName, ssn, university, new AdminUser());
        return userRepository.save(user);
    }

    private University getUniversity(String universityName) {
        return universityRepository.findByNameIgnoreCase(universityName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown university."));
    }

}