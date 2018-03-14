package nl.quintor.studybits.student.services;

import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.student.interfaces.StudentRepository;
import nl.quintor.studybits.student.model.MetaWallet;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
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
    public Optional<Student> findById(Long studentId) {
        return studentRepository
                .findById(studentId);
    }

    @SneakyThrows
    void checkIfPresentOrElseThrow(Long studentId) {
        studentRepository
                .findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student with username not found."));
    }

    @SneakyThrows
    public Student createAndSave(String username, String uniName) {
        if (studentRepository.findByUsername(username).isPresent())
            throw new IllegalArgumentException("Student with username exists already.");

        if (!universityService.exists(uniName))
            throw new NotFoundException("University does not exist (yet).");

        University university = universityService.getByName(uniName);
        MetaWallet metaWallet = metaWalletService.createAndSave(username);
        Student student = new Student(null, username, university, metaWallet);

        return studentRepository.save(student);
    }
}

