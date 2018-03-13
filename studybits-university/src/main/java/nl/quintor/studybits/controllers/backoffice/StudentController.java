package nl.quintor.studybits.controllers.backoffice;

import nl.quintor.studybits.models.Student;
import nl.quintor.studybits.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@BackOfficeRestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/")
    List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }
    

}
