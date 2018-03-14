package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.student.model.MetaWallet;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StudentService {
    private StudentRepository studentRepository;
    private UniversityService universityService;
    private MetaWalletService metaWalletService;

    @SneakyThrows
    public Student createAndSave(String username, String uniName) {
        if (studentRepository.existsByUsername(username))
            throw new IllegalArgumentException("Student with username exists already.");

        University university = universityService
                .findByName(uniName)
                .orElseThrow(() -> new IllegalArgumentException("University with id not found"));
        MetaWallet metaWallet = metaWalletService.createAndSave(username);
        Student student = new Student(null, username, university, metaWallet);

        return studentRepository.save(student);
    }

    public Optional<Student> findById(Long studentId) {
        return studentRepository
                .findById(studentId);
    }

    public Optional<Student> findByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    public Student updateById(Long studentId, Student student) {
        if (!studentRepository.existsById(studentId))
            throw new IllegalArgumentException("Student with id not found.");

        student.setId(studentId);
        return studentRepository.save(student);
    }
}

