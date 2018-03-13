package nl.quintor.studybits;

import nl.quintor.studybits.entities.Student;
import nl.quintor.studybits.entities.University;
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
    @Qualifier("universityTrustAnchor")
    private TrustAnchor rug;

    @EventListener
    public void seed(ContextRefreshedEvent event) {
        if(isEmpty()) {
            List<University> universities = seedUniversities();
            Map<String, University> univerityMap = universities
                    .stream()
                    .collect(Collectors.toMap(x -> x.getName(),x -> x ));
            seedStudents(univerityMap);
            studentRepository.flush();
        }
    }

    public Boolean isEmpty() {
        return studentRepository.findAll().isEmpty();
    }

    private List<University> seedUniversities() {
        University u1 = createUniversity("rug");
        University u2 = createUniversity("gent");
        List<University> universities = Arrays.asList(u1, u2);
        return universityRepository.saveAll(universities);
    }

    private List<Student> seedStudents(Map<String, University> universities) {
        University rug = universities.get("rug");
        Student s1 = createStudent("student1", "Cor", "Nuiten", rug, "2017/18");
        Student s2 = createStudent("student2", "Connie", "Veren", rug, "2016/17", "2017/18");
        Student s3 = createStudent("student2", "Fokje", "Modder", rug, "2016/17", "2017/18");
        List<Student> students = Arrays.asList(s1, s2, s3);
        return studentRepository.saveAll(students);
    }

    private University createUniversity(String name) {
        return new University(null, name, null, new HashSet<>());
    }

    private Student createStudent(String userName, String firstName, String lastName, University university, String... academicYears) {
        Set<String> years = academicYears == null ? new HashSet<>() :new HashSet<>(Arrays.asList(academicYears));
        return new Student(null, userName, firstName, lastName, university, null, years);
    }


}
