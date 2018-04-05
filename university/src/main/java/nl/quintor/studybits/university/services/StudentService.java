package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.UserModel;
import nl.quintor.studybits.university.repositories.UniversityRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StudentService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final Mapper mapper;

    private User toEntity(UserModel userModel) {
        return mapper.map(userModel, User.class);
    }

    public List<User> findAllStudents() {
        return userRepository
                .findAllByStudentUserIsNotNull();
    }

    public List<User> findAllForUniversity(String universityName) {
        return userRepository.findAllByStudentUserIsNotNullAndUniversityNameIgnoreCase(universityName);
    }

    public Optional<User> findByUniversityAndUserName(String universityName, String userName) {
        return userRepository
                .findAllByStudentUserIsNotNullAndUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName);
    }
}