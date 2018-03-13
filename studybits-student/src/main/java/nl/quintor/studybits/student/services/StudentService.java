package nl.quintor.studybits.student.services;

import javassist.NotFoundException;
import nl.quintor.studybits.student.interfaces.StudentRepository;
import nl.quintor.studybits.student.model.MetaWallet;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UniversityService universityService;

    @Autowired
    private MetaWalletService metaWalletService;

    public Boolean exists(String username) {
        return studentRepository.getByUsername(username) != null;
    }

    public Student getById(Long studentId) {
        return studentRepository.getById(studentId);
    }

    public Student createAndSave(String username, String uniName) throws Exception {
        if (exists(username))
            throw new IllegalArgumentException("Student with username exists already.");

        if (!universityService.exists(uniName))
            throw new NotFoundException("University does not exist (yet).");

        University university = universityService.getByName(uniName);
        MetaWallet metaWallet = metaWalletService.createAndSave(username);
        Student student = new Student(null, username, university, metaWallet);

        return studentRepository.save(student);
    }
}
