package nl.quintor.studybits;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.dto.Enrolment;
import nl.quintor.studybits.dto.SchemaUtils;
import nl.quintor.studybits.entities.Student;
import nl.quintor.studybits.entities.University;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import nl.quintor.studybits.repositories.StudentRepository;
import nl.quintor.studybits.repositories.UniversityRepository;
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
    private StudentRepository studentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private Issuer[] issuers;

    @EventListener
    public void seed(ContextRefreshedEvent event) {
        if(isEmpty()) {
            log.info("Seeding started...");
            List<University> universities = seedUniversities();
            Map<String, University> universityMap = convertToMap(universities, x -> x.getName().toLowerCase());
            seedStudents(universityMap);
            seedClaimDefinitions("rug", Enrolment.class);
            log.info("Seeding completed.");
        }
    }

    public Boolean isEmpty() {
        return studentRepository.findAll().isEmpty();
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
        Arrays.stream(claimTypes).map(SchemaUtils::getSchemaDefinition)
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
                .collect(Collectors.toMap(keySelectFunction::apply, x -> x));
    }

    private List<Student> seedStudents(Map<String, University> universityMap) {
        University rug = universityMap.get("rug");
        Student s1 = createStudent("student1", "Cor", "Nuiten", rug, "2017/18");
        Student s2 = createStudent("student2", "Connie", "Veren", rug, "2016/17", "2017/18");
        Student s3 = createStudent("student2", "Fokje", "Modder", rug, "2016/17", "2017/18");
        List<Student> students = Arrays.asList(s1, s2, s3);
        return studentRepository.saveAll(students);
    }

    private University createUniversity(String name) {

        log.info("Creating {} university...", name);
        return new University(null, name, new HashSet<>());
    }

    private Student createStudent(String userName, String firstName, String lastName, University university, String... academicYears) {
        log.info("Creating student {} for university {}...", userName, university.getName());
        Set<String> years = academicYears == null ? new HashSet<>() :new HashSet<>(Arrays.asList(academicYears));
        return new Student(null, userName, firstName, lastName, university, null, years);
    }

}