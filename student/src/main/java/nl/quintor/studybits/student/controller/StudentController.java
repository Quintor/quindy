package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    @PostMapping("/onboard")
    void onboard(@RequestParam String studentUserName, @RequestParam String universityName) throws Exception {
        studentService.onboard(studentUserName, universityName);
    }

    @PostMapping("/register")
    Student register(@RequestParam String username, @RequestParam String universityName) {
        return studentService.createAndSave(username, universityName);
    }

    @GetMapping("/{studentUserName}")
    Student findById(@PathVariable String studentUserName) {
        return studentService.findByNameOrElseThrow(studentUserName);
    }

    @GetMapping
    List<Student> findAll() {
        return studentService.findAll();
    }

    @PutMapping
    void updateByObject(@RequestBody Student student) {
        studentService.updateByObject(student);
    }

    @DeleteMapping("/{studentUserName}")
    void deleteById(@PathVariable String studentUserName) {
        studentService.deleteByUserName(studentUserName);
    }
}
