package nl.quintor.studybits;

import nl.quintor.studybits.entities.Student;
import nl.quintor.studybits.entities.University;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.TrustAnchor;
import nl.quintor.studybits.repositories.StudentRepository;
import nl.quintor.studybits.repositories.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Component
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
            List<University> universities = seedUniversities();
            seedStudents(universities);
            studentRepository.flush();
        }
    }

    public Boolean isEmpty() {
        return studentRepository.findAll().isEmpty();
    }

    private List<University> seedUniversities() {
        List<University> universities = Arrays
                .stream(issuers)
                .map(x -> createUniversity(x.getName()))
                .collect(Collectors.toList());
        return universityRepository.saveAll(universities);
    }

    private List<Student> seedStudents(List<University> universities) {
        University rug = universities.get(0);
        Student s1 = createStudent("student1", "Cor", "Nuiten", rug, "2017/18");
        Student s2 = createStudent("student2", "Connie", "Veren", rug, "2016/17", "2017/18");
        Student s3 = createStudent("student2", "Fokje", "Modder", rug, "2016/17", "2017/18");
        List<Student> students = Arrays.asList(s1, s2, s3);
        return studentRepository.saveAll(students);
    }

    private University createUniversity(String name) {
        return new University(null, name, new HashSet<>());
    }

    private Student createStudent(String userName, String firstName, String lastName, University university, String... academicYears) {
        Set<String> years = academicYears == null ? new HashSet<>() :new HashSet<>(Arrays.asList(academicYears));
        return new Student(null, userName, firstName, lastName, university, null, years);
    }


}
