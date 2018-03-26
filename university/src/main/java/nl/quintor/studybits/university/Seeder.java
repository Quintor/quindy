package nl.quintor.studybits.university;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Seeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private Issuer[] issuers;

    @Autowired
    private EnrolmentService enrolmentService;

    @Autowired
    private TranscriptService transcriptService;

    @EventListener
    public void seed(ContextRefreshedEvent event) {
        if(isEmpty()) {
            log.info("Seeding started...");
            List<University> universities = seedUniversities();
            seedClaimDefinitions("rug", Enrolment.class);
            seedClaimDefinitions("rug", Transcript.class);
            Map<String, University> universityMap = convertToMap(universities, x -> x.getName().toLowerCase());
            seedAdmins(universityMap);
            List<User> students = seedStudents(universityMap);
            addAcademicYears(students.get(0), "2016/17");
            addAcademicYears(students.get(1), "2016/17", "2017/18");
            addAcademicYears(students.get(2), "2015/16", "2016/17", "2017/18");
            addTranscript(students.get(2), new TranscriptModel("Bachelor of Science, Marketing", "graduated", "2018", "5"));
            log.info("Seeding completed.");
        }
    }

    public Boolean isEmpty() {
        return userRepository.findAll().isEmpty();
    }

    private Optional<Issuer> getIssuerByName(String name) {
        return Arrays.stream(issuers)
                .filter(i -> name.equalsIgnoreCase(i.getName()))
                .findFirst();
    }

    private List<University> seedUniversities() {
        List<University> universities = Arrays
                .stream(issuers)
                .map(x -> createUniversity(x.getName()))
                .collect(Collectors.toList());
        return universityRepository.saveAll(universities);
    }

    private void seedClaimDefinitions(String universityName, Class<?>... claimTypes) {
        Issuer issuer = getIssuerByName(universityName)
               .orElseThrow(() -> new IllegalStateException(String.format("Issuer for %s university not found!", universityName)));
        Arrays.stream(claimTypes).map(ClaimUtils::getSchemaDefinition)
                .forEach(schemaDefinition -> defineSchema(issuer, schemaDefinition));
    }

    @SneakyThrows
    private void defineSchema(Issuer issuer, SchemaDefinition schemaDefinition) {
        log.info("Issuer {} defining schema definition '{}', version: '{}'.", issuer.getName(), schemaDefinition.getName(), schemaDefinition.getVersion());
        SchemaKey schemaKey = issuer.createAndSendSchema(schemaDefinition).get();
        issuer.defineClaim(schemaKey).get();
    }


    private <T> Map<String, T> convertToMap(List<T> items, Function<T, String> keySelectFunction) {
        return items.stream()
                .collect(Collectors.toMap(keySelectFunction, x -> x));
    }

    private List<User> seedStudents(Map<String, University> universityMap) {
        University rug = universityMap.get("rug");
        User rugStudent1 = createStudent("student1", "Peter", "Ulrich", "1111-11-0001", rug);
        User rugStudent2 = createStudent("student2", "Margot", "Veren", "1111-11-0002", rug);
        User rugStudent3 = createStudent("student3", "Ko", "de Kraker", "1111-11-0003", rug);
        University gent = universityMap.get("gent");
        User gentStudent1 = createStudent("student1", "Axelle", "Wanders", "1111-22-0001", gent);
        User gentStudent2 = createStudent("student2", "Laure", "de Vadder", "1111-22-0002", gent);
        User gentStudent3 = createStudent("student3", "Senne", "de Waal", "1111-22-0003", gent);
        List<User> users = Arrays.asList(rugStudent1, rugStudent2, rugStudent3, gentStudent1, gentStudent2, gentStudent3);
        return userRepository.saveAll(users);
    }

    private List<User> seedAdmins(Map<String, University> universityMap) {
        University rug = universityMap.get("rug");
        User admin1 = createAdmin("admin1", "Etienne", "Nijboer", "222-11-0001", rug);
        University gent = universityMap.get("gent");
        User admin2 = createAdmin("admin2", "Pim", "Otten", "222-22-0002", gent);
        List<User> users = Arrays.asList(admin1, admin2);
        return userRepository.saveAll(users);
    }

    private University createUniversity(String name) {
        log.info("Creating {} university...", name);
        return new University(null, name, new HashSet<>());
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
        if(studentUser != null) {
            studentUser.setUser(user);
        }
        if(adminUser != null) {
            adminUser.setUser(user);
        }
        return user;
    }

    private void addAcademicYears(User user, String... academicYears) {
        Arrays.stream(academicYears)
                .forEach(academicYear -> enrolmentService.addEnrolment(user.getId(), academicYear));
    }

    private void addTranscript(User user, TranscriptModel transcriptModel) {
        transcriptService.addTranscript(user.getId(), transcriptModel);
    }

}