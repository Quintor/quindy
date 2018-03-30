package nl.quintor.studybits.university.controllers.admin;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.UserModel;
import nl.quintor.studybits.university.services.StudentService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/admin/{userName}/students")
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class StudentController {

    private final UserContext userContext;
    private final StudentService studentService;
    private final Mapper mapper;

    private UserModel toModel(User user) {
        return mapper.map(user, UserModel.class);
    }

    @GetMapping
    List<UserModel> findAll() {
        return studentService
                .findAllForUniversity(userContext.currentUniversityName())
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/{studentUserName}")
    UserModel findByUserName(@PathVariable String studentUserName) {
        return studentService.findByUniversityAndUserName(userContext.currentUniversityName(), studentUserName)
                .map(this::toModel)
                .orElseThrow(() -> new IllegalArgumentException("user name not found for university."));
    }

    @PostMapping
    UserModel createStudent(@RequestBody UserModel userModel) {
        return toModel(studentService.createStudent(userContext.currentUniversityName(), userModel));
    }

}