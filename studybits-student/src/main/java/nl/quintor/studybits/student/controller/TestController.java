package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.Main;
import nl.quintor.studybits.student.services.MetaWalletService;
import nl.quintor.studybits.student.services.StudentService;
import nl.quintor.studybits.student.services.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/test")
public class TestController {
    private final StudentService studentService;
    private final MetaWalletService metaWalletService;
    private final UniversityService universityService;
    @DeleteMapping("/nuke")
    void nuke() throws Exception {
        studentService.deleteAll();
        universityService.deleteAll();
        metaWalletService.deleteAll();
        Main.removeIndyClientDirectory();
    }
}
