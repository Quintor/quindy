package nl.quintor.studybits.services;

import nl.quintor.studybits.models.Student;
import nl.quintor.studybits.repositories.StudentRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private Mapper mapper;

    private Student toModel(Object student) {
        return mapper.map(student, Student.class);
    }

    public List<Student> findAllStudents() {
        return studentRepository
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Student> findAllForUniversity(String universityName) {
        return studentRepository
                .findAllByUniversityNameIgnoreCase(universityName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public Optional<Student> findById(Long id) {
        return studentRepository
                .findById(id)
                .map(this::toModel);
    }

    public Optional<Student> findByUniversityAndUserName(String universityName, String userName) {
        return studentRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName)
                .map(this::toModel);
    }


}
