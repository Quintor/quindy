package nl.quintor.studybits.university.controllers.test;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.Seeder;
import nl.quintor.studybits.university.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Autowired
    private AdminUserRepository adminUserRepository;
    @Autowired
    private ClaimRecordRepository claimRecordRepository;
    @Autowired
    private StudentUserRepository studentUserRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Seeder seeder;

    @DeleteMapping("/nuke")
    public void nuke() {
        log.debug("Deleting admin users");
        adminUserRepository.deleteAll();
        log.debug("Deleting claim records");
        claimRecordRepository.deleteAll();
        log.debug("Deleting student users");
        studentUserRepository.deleteAll();
        log.debug("Deleting users");
        userRepository.deleteAll();
        log.debug("Seeding");
        seeder.seed(false);
    }

    @GetMapping("/health")
    String health() {
        return "University Backend says: Ich lebe! Heidewitzka!";
    }
}
