package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    @PostMapping("/onboard")
    void onboard(@RequestParam Student student, @RequestParam University university) throws Exception {
        studentService.onboard(student, university);
    }

    @PostMapping("/register")
    Student register(@RequestParam String username, @RequestParam(value = "university") String uniName) {
        return studentService.createAndSave(username, uniName);
    }

    @GetMapping("/{studentId}")
    Student findById(@PathVariable Long studentId) {
        return studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));
    }

    @GetMapping()
    List<Student> findAll(@RequestParam("name") String name) {
        if (name != null) {
            return studentService.findByName(name)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }
        return studentService.findAll();
    }

    @PutMapping()
    void updateById(@RequestBody Student student) {
        studentService.updateById(student);
    }

    @DeleteMapping("/{studentId}")
    void deleteById(@PathVariable Long studentId) {
        studentService.deleteById(studentId);
    }


}
