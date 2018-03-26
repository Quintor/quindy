package nl.quintor.studybits.university.controllers.test;

import nl.quintor.studybits.university.Seeder;
import nl.quintor.studybits.university.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private AdminUserRepository adminUserRepository;
    @Autowired
    private ClaimRecordRepository claimRecordRepository;
    @Autowired
    private StudentClaimRepository studentClaimRepository;
    @Autowired
    private StudentUserRepository studentUserRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Seeder seeder;

    @DeleteMapping("/nuke")
    public void nuke() {
        adminUserRepository.deleteAll();
        studentClaimRepository.deleteAll();
        claimRecordRepository.deleteAll();
        studentUserRepository.deleteAll();
        userRepository.deleteAll();
        seeder.seed(false);
    }

    @GetMapping("/health")
    String health() {
        return "University Backend says: Ich lebe! Heidewitzka!";
    }
}
