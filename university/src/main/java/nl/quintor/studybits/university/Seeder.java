package nl.quintor.studybits.university;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import nl.quintor.studybits.university.dto.*;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.TranscriptModel;
import nl.quintor.studybits.university.repositories.UserRepository;
import nl.quintor.studybits.university.services.EnrolmentService;
import nl.quintor.studybits.university.services.TranscriptService;
import nl.quintor.studybits.university.services.UniversityService;
import nl.quintor.studybits.university.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class Seeder {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UniversityService universityService;
    private final EnrolmentService enrolmentService;
    private final TranscriptService transcriptService;

    @EventListener
    public void seed(ContextRefreshedEvent event) {
        if (isEmpty()) {
            seed(true);
        }
    }

    public void seed(boolean withLedger) {
        log.info("Seeding started...");
        if (withLedger) {
            seedUniversities();
        }
        seedUsers();
        log.info("Seeding completed.");
    }

    private Boolean isEmpty() {
        return userService.findAll().isEmpty();
    }

    private void seedUniversities() {
        universityService.create("Rug");
        universityService.create("Gent");

        SchemaDefinition enrolmentSchemaDefinition = ClaimUtils.getSchemaDefinition(Enrolment.class);
        SchemaKey enrolmentSchemaKey = universityService.defineSchema("rug", enrolmentSchemaDefinition);
        universityService.defineClaim("rug", enrolmentSchemaDefinition);
        universityService.addSchema("gent", enrolmentSchemaKey);

        SchemaDefinition transcriptSchemaDefinition = ClaimUtils.getSchemaDefinition(Transcript.class);
        SchemaKey transcriptSchemaKey = universityService.defineSchema("rug", transcriptSchemaDefinition);
        universityService.defineClaim("rug", transcriptSchemaDefinition);
        universityService.addSchema("gent", transcriptSchemaKey);

        exchangeUniversityClaimIssuerSchemaInfo("gent", "rug");
        exchangeUniversityClaimIssuerSchemaInfo("rug", "gent");
    }

    private void exchangeUniversityClaimIssuerSchemaInfo(String universityName, String universityIssuerName) {
        UniversityIssuer universityIssuer = universityService.getUniversityIssuer(universityIssuerName);
        universityIssuer.getDefinedSchemaKeys()
                .forEach(schemaKey -> universityService
                        .addClaimIssuerForSchema(universityName, new ClaimIssuerSchema(universityIssuerName, universityIssuer.getUniversityDid(), schemaKey)));
    }


    private void seedUsers() {
        userService.createAdmin("rug","admin1", "Etienne", "Nijboer", "222-11-0001", true);

        User rugStudent1 = userService.createStudent("rug","peter", "Peter", "Ullrich", "1111-11-0001", true);
        enrolmentService.addEnrolment(rugStudent1.getId(), "2016/17");
        enrolmentService.addEnrolment(rugStudent1.getId(), "2017/18");

        User rugStudent2 = userService.createStudent("rug","lisa", "Lisa", "Veren", "1111-11-0002", true);
        enrolmentService.addEnrolment(rugStudent2.getId(), "2015/16");
        enrolmentService.addEnrolment(rugStudent2.getId(), "2016/17");
        transcriptService.addTranscript(rugStudent2.getId(), new TranscriptModel("Bachelor of Science, Marketing", "graduated", "2017", "8"));

        User rugStudent3 = userService.createStudent("rug","johan", "Johan", "Tillema", "1111-11-0003", true);
        enrolmentService.addEnrolment(rugStudent3.getId(), "2015/16");
        enrolmentService.addEnrolment(rugStudent3.getId(), "2016/17");
        enrolmentService.addEnrolment(rugStudent3.getId(), "2017/18");
        transcriptService.addTranscript(rugStudent2.getId(), new TranscriptModel("Bachelor of Science, Computer Science", "graduated",
                "1980", "10"));

        userService.createStudent("gent","admin2-gent", "Pim", "Otte", "222-22-0002", true);
        userService.createStudent("gent","student1-gent", "Axelle", "Wanders", "1111-22-0001", true);
        userService.createStudent("gent","student2-gent", "Laure", "de Vadder", "1111-22-0002", true);
        userService.createStudent("gent","student3-gent", "Senne", "de Waal", "1111-22-0003", true);
        userService.createStudent("gent","student4-gent", "Jan", "Jansen", "7777-88-0001", false);

    }

}