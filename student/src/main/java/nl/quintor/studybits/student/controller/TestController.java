package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.Seeder;
import nl.quintor.studybits.student.repositories.*;
import nl.quintor.studybits.student.services.MetaWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/test")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TestController {
    private final Seeder seeder;
    private final ClaimRepository claimRepository;
    private final ConnectionRecordRepository connectionRecordRepository;
    private final MetaWalletService metaWalletService;
    private final MetaWalletRepository metaWalletRepository;
    private final ProofRequestRecordRepository proofRequestRecordRepository;
    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;

    @DeleteMapping("/nuke")
    public void nuke() {
        log.info("Deleting claims");
        claimRepository.deleteAll();
        log.info("Deleting connection records");
        connectionRecordRepository.deleteAll();
        log.info("Deleting proof request records");
        proofRequestRecordRepository.deleteAll();
        log.info("Deleting meta wallets");
        metaWalletService.deleteAll();
        log.info("Deleting students");
        studentRepository.deleteAll();
        log.info("Deleting universities");
        universityRepository.deleteAll();
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