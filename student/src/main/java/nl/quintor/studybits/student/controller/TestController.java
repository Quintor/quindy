package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.Seeder;
import nl.quintor.studybits.student.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/test")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TestController {
    private final StudentService studentService;
    private final MetaWalletService metaWalletService;
    private final UniversityService universityService;
    private final ConnectionRecordService connectionRecordService;
    private final ClaimService claimService;
    private final ProofRequestService proofRequestService;
    private final Seeder seeder;

    // TODO: Replace with Repositories and delete deleteAll() functions in Services
    @DeleteMapping("/nuke")
    public void nuke() throws Exception {
        log.info("Deleting connection records");
        connectionRecordService.deleteAll();
        log.info("Deleting claims");
        claimService.deleteAll();
        log.info("Deleting proof request records");
        proofRequestService.deleteAll();
        log.info("Deleting students");
        studentService.deleteAll();
        log.info("Deleting meta wallets");
        metaWalletService.deleteAll();
        log.info("Deleting universities");
        universityService.deleteAll();
    }

    @PostMapping("/seed")
    void seed() {
        seeder.seed();
    }

    @GetMapping("/health")
    String health() {
        return "StudentModel Backend says: Ich lebe! Heidewitzka!";
    }

}