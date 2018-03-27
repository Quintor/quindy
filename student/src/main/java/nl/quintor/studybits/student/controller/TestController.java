package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    private final StudentService studentService;
    private final MetaWalletService metaWalletService;
    private final UniversityService universityService;
    private final ConnectionRecordService connectionRecordService;
    private final ClaimRecordService claimRecordService;

    @DeleteMapping("/nuke")
    void nuke() throws Exception {
        log.info("Deleting connection records");
        connectionRecordService.deleteAll();
        log.info("Deleting claim records");
        claimRecordService.deleteAll();
        log.info("Deleting students");
        studentService.deleteAll();
        log.info("Deleting meta wallets");
        metaWalletService.deleteAll();
        log.info("Deleting universities");
        universityService.deleteAll();
    }

    @GetMapping("/health")
    String health() {
        return "Student Backend says: Ich lebe! Heidewitzka!";
    }
}
