package nl.quintor.studybits.university;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.university.dto.Claim;
import nl.quintor.studybits.university.dto.ClaimUtils;
import nl.quintor.studybits.university.dto.Enrolment;
import nl.quintor.studybits.university.dto.Transcript;
import nl.quintor.studybits.university.entities.AdminUser;
import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.TranscriptModel;
import nl.quintor.studybits.university.repositories.UniversityRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import nl.quintor.studybits.university.services.EnrolmentService;
import nl.quintor.studybits.university.services.TranscriptService;
import nl.quintor.studybits.university.services.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class Seeder {

    private final UserRepository userRepository;
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

    public Boolean isEmpty() {
        return userRepository.findAll().isEmpty();
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
    }


    private void seedUsers() {
        University rug = universityService.getUniversity("rug");

        User admin1 = createAdmin("admin1", "Etienne", "Nijboer", "222-11-0001", rug);

        User rugStudent1 = createStudent("student1", "Peter", "Ullrich", "1111-11-0001", rug);
        addAcademicYears(rugStudent1, "2016/17");

        User rugStudent2 = createStudent("student2", "Margot", "Veren", "1111-11-0002", rug);
        addAcademicYears(rugStudent2, "2016/17", "2017/18");

        User rugStudent3 = createStudent("student3", "Ko", "de Kraker", "1111-11-0003", rug);
        addAcademicYears(rugStudent3, "2015/16", "2016/17", "2017/18");
        addTranscript(rugStudent3, new TranscriptModel("Bachelor of Science, Marketing", "graduated", "2018", "5"));

        University gent = universityService.getUniversity("gent");
        User admin2 = createAdmin("admin2", "Pim", "Otte", "222-22-0002", gent);
        User gentStudent1 = createStudent("student1", "Axelle", "Wanders", "1111-22-0001", gent);
        User gentStudent2 = createStudent("student2", "Laure", "de Vadder", "1111-22-0002", gent);
        User gentStudent3 = createStudent("student3", "Senne", "de Waal", "1111-22-0003", gent);
        List<User> users = Arrays.asList(admin1, rugStudent1, rugStudent2, rugStudent3, admin2, gentStudent1, gentStudent2, gentStudent3);
        userRepository.saveAll(users);
    }


    private User createStudent(String userName, String firstName, String lastName, String ssn, University university) {
        log.info("Creating admin user {} for university {}...", userName, university.getName());
        StudentUser studentUser = new StudentUser(null, null, new HashSet<>(), new ArrayList<>());
        return createUser(userName, firstName, lastName, ssn, university, studentUser, null);
    }

    private User createAdmin(String userName, String firstName, String lastName, String ssn, University university) {
        log.info("Creating admin user {} for university {}...", userName, university.getName());
        return createUser(userName, firstName, lastName, ssn, university, null, new AdminUser());
    }

    private User createUser(String userName, String firstName, String lastName, String ssn, University university, StudentUser studentUser, AdminUser adminUser) {
        log.info("Creating admin user {} for university {}...", userName, university.getName());
        User user = new User(null, userName, firstName, lastName, ssn, university, null, new ArrayList<>(), studentUser, adminUser);
        if (studentUser != null) {
            studentUser.setUser(user);
        }
        if (adminUser != null) {
            adminUser.setUser(user);
        }
        return userRepository.save(user);
    }

    private void addAcademicYears(User user, String... academicYears) {
        Arrays.stream(academicYears)
                .forEach(academicYear -> enrolmentService.addEnrolment(user.getId(), academicYear));
    }

    private void addTranscript(User user, TranscriptModel transcriptModel) {
        transcriptService.addTranscript(user.getId(), transcriptModel);
    }

}