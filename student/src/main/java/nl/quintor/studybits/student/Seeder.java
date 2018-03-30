package nl.quintor.studybits.student;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.controller.TestController;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.repositories.MetaWalletRepository;
import nl.quintor.studybits.student.services.StudentService;
import nl.quintor.studybits.student.services.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class Seeder {

    @Autowired
    private UniversityService universityService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TestController testController;

    @Autowired
    private MetaWalletRepository metaWalletRepository;
    @EventListener
    public void seed(ContextRefreshedEvent event) throws Exception{
        if (isEmpty()) {
            log.info("Seeding");
            seed();
        }
    }

    public void seed() {
        log.info("Seeding started...");
        seedUniversities();
        seedStudents();
        log.info("Seeding completed.");
    }

    public Boolean isEmpty() {
        return studentService.findAll().isEmpty();
    }


    private List<University> seedUniversities() {
        University rug = universityService.createAndSave("rug", "http://localhost:8090");
        University gent = universityService.createAndSave("gent", "http://localhost:8090");

        return Arrays.asList(rug, gent);
    }


    private List<Student> seedStudents() {
        log.info("Creating student 1");
        Student rugStudent1 = studentService.createAndSave("student1", "rug");
        log.info("Creating student 2");
        Student rugStudent2 = studentService.createAndSave("student2", "rug");
        log.info("Creating student 3");
        Student rugStudent3 = studentService.createAndSave("student3", "rug");
        List<Student> students = Arrays.asList(rugStudent1, rugStudent2, rugStudent3);
        return students;
    }


}