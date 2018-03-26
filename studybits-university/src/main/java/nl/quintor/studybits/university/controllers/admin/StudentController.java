package nl.quintor.studybits.university.controllers.admin;

import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.models.UserModel;
import nl.quintor.studybits.university.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{universityName}/admin/{userName}/students")
public class StudentController {

    @Autowired
    private UserContext userContext;

    @Autowired
    private StudentService studentService;

    @GetMapping("")
    List<UserModel> findAll() {
        return studentService.findAllForUniversity(userContext.currentUniversityName());
    }

    @GetMapping("/{studentUserName}")
    UserModel findByUserName(@PathVariable String studentUserName) {
        return studentService.findByUniversityAndUserName(userContext.currentUniversityName(), studentUserName)
                .orElseThrow(() -> new IllegalArgumentException("UserModel user name not found for university."));
    }

    @PostMapping("")
    UserModel createStudent(@RequestBody UserModel userModel) {
        return studentService.createStudent(userContext.currentUniversityName(), userModel);
    }

}