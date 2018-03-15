package nl.quintor.studybits.controllers.admin;

import nl.quintor.studybits.models.Student;
import nl.quintor.studybits.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{universityName}/admin/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("")
    List<Student> findAll(@PathVariable String universityName) {
        return studentService.findAllForUniversity(universityName);
    }

    @GetMapping("/{userName}")
    Student findByUserName(@PathVariable String universityName, @PathVariable String userName) {
        return studentService.findByUniversityAndUserName(universityName, userName)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for university."));
    }


}
