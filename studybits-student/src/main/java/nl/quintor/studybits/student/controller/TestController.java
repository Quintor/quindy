package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.Main;
import nl.quintor.studybits.student.services.*;
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
    private final ConnectionRecordService connectionRecordService;
    private final ClaimRecordService claimRecordService;

    @DeleteMapping("/nuke")
    void nuke() throws Exception {
        connectionRecordService.deleteAll();
        claimRecordService.deleteAll();
        studentService.deleteAll();
        universityService.deleteAll();
        metaWalletService.deleteAll();
        Main.removeIndyClientDirectory();
    }
}
