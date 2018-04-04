package nl.quintor.studybits.university.controllers.student;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.UserModel;
import nl.quintor.studybits.university.services.UserService;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/{universityName}/student/{userName}/students")
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class StudentController {

    private final UserContext userContext;
    private final UserService userService;
    private final Mapper mapper;

    private UserModel toModel(User user) {
        return mapper.map(user, UserModel.class);
    }


    @GetMapping
    UserModel getStudent() {
        User user = userService.getById(userContext.currentUserId());
        return toModel(user);
    }

    @PostMapping
    UserModel createStudent(@RequestBody UserModel userModel) {
        Validate.isTrue(!userContext.getCurrentUser().isPresent(), "User already exists.");
        Validate.isTrue(userContext.currentUserName().equalsIgnoreCase(userModel.getUserName()), "UserName mismatch.");
        User user = userService
                .createStudent(
                        userContext.currentUniversityName(),
                        userModel.getUserName(),
                        userModel.getFirstName(),
                        userModel.getLastName(),
                        userModel.getSsn(),
                        false);
        return toModel(user);
    }
}