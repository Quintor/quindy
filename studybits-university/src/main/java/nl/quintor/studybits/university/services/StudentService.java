package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.models.Student;
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
    private UserContext userContext;

    @Autowired
    private Mapper mapper;

    private Student toModel(Object student) {
        return mapper.map(student, Student.class);
    }

    public List<Student> findAllStudents() {
        return userRepository
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Student> findAllForUniversity(String universityName) {
        return userRepository
                .findAllByUniversityNameIgnoreCase(universityName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }


    public Optional<Student> findById(Long id) {
        return userRepository
                .findById(id)
                .map(this::toModel);
    }

    public Optional<Student> findByUniversityAndUserName(String universityName, String userName) {
        return userRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName)
                .map(this::toModel);
    }

}