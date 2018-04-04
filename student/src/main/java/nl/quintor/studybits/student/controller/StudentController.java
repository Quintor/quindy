package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.model.StudentModel;
import nl.quintor.studybits.student.services.StudentService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/student")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StudentController {
    private StudentService studentService;
    private Mapper mapper;

    private StudentModel toModel(Object student) {
        return mapper.map(student, StudentModel.class);
    }

    @PostMapping("/onboard")
    void onboard(@RequestParam String studentUserName, @RequestParam String universityName) throws Exception {
        studentService.onboard(studentUserName, universityName);
    }

    @PostMapping("/register")
    StudentModel register(@RequestParam String username, @RequestParam String universityName) {
        return toModel(studentService.createAndSave(username, universityName));
    }

    @GetMapping("/{studentUserName}")
    StudentModel findById(@PathVariable String studentUserName) {
        return toModel(studentService.findByNameOrElseThrow(studentUserName));
    }

    @GetMapping
    List<StudentModel> findAll() {
        return studentService
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @PutMapping
    void updateByObject(@RequestBody StudentModel studentModel) {
        Student student = mapper.map(studentModel, Student.class);
        studentService.updateByObject(student);
    }

    @DeleteMapping("/{studentUserName}")
    void deleteById(@PathVariable String studentUserName) {
        studentService.deleteByUserName(studentUserName);
    }
}
