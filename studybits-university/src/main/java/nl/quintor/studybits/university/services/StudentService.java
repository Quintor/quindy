package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.models.UserModel;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Mapper mapper;

    private UserModel toModel(Object user) {
        return mapper.map(user, UserModel.class);
    }

    public List<UserModel> findAllStudents() {
        return userRepository
                .findAllByStudentUserIsNotNull()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<UserModel> findAllForUniversity(String universityName) {
        return userRepository
                .findAllByStudentUserIsNotNullAndUniversityNameIgnoreCase(universityName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }


    public Optional<UserModel> findById(Long id) {
        return userRepository
                .findById(id)
                .map(this::toModel);
    }

    public Optional<UserModel> findByUniversityAndUserName(String universityName, String userName) {
        return userRepository
                .findAllByStudentUserIsNotNullAndUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName)
                .map(this::toModel);
    }

}