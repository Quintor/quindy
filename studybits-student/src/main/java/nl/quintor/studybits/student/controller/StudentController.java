package nl.quintor.studybits.student.controller;

import javassist.NotFoundException;
import nl.quintor.studybits.student.interfaces.ClaimRepository;
import nl.quintor.studybits.student.interfaces.StudentRepository;
import nl.quintor.studybits.student.interfaces.UniversityRepository;
import nl.quintor.studybits.student.model.Claim;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UniversityRepository universityRepository;
    @Autowired
    private ClaimRepository claimRepository;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Student register(@RequestParam(value = "username") String username, @RequestParam(value = "university") String uniName) throws Exception {
        if (studentRepository.getByUsername(username) != null)
            throw new NotFoundException("Student with username exists already.");

        University university = universityRepository.getByName(uniName);
        if (university == null)
            throw new NotFoundException("University does not exist (yet).");

        Student student = new Student(username, university);
        studentRepository.save(student);

        return student;
    }

    @RequestMapping(value = "/{studentId}/claims", method = RequestMethod.GET)
    public List<Claim> getClaims(@PathVariable Long studentId) throws Exception {
        Student student = studentRepository.getById(studentId);
        if (student == null)
            throw new NotFoundException("Student with username not found. Maybe register first.");

        return claimRepository.findAllByOwner(student);
    }

    @RequestMapping(value = "/{studentId}/claims/{claimId}", method = RequestMethod.GET)
    public Claim getClaimById(@PathVariable Long studentId, @PathVariable Long claimId) throws Exception {
        Claim claim = claimRepository.getById(claimId);
        if (claim.getOwner().getId().equals(studentId))
            throw new NotFoundException("Claim with id not found for student.");

        return claim;
    }
}
