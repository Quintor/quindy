package nl.quintor.studybits.university.controllers.test;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.Seeder;
import nl.quintor.studybits.university.repositories.ExchangePositionRepository;
import nl.quintor.studybits.university.repositories.SchemaDefinitionRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TestController {

    private final UserRepository userRepository;
    private final ExchangePositionRepository exchangePositionRepository;
    private final SchemaDefinitionRepository schemaDefinitionRepository;
    private final Seeder seeder;

    @DeleteMapping("/nuke")
    public void nuke() {
        log.debug("Deleting users");
        userRepository.deleteAll();
        log.debug("Seeding");
        seeder.seed(false);
    }

    @GetMapping("/health")
    String health() {
        return "UniversityModel Backend says: Ich lebe! Heidewitzka!";
    }
}