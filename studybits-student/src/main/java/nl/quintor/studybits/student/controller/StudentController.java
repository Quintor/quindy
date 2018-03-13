package nl.quintor.studybits.student.controller;

import javassist.NotFoundException;
import nl.quintor.studybits.student.interfaces.ClaimOfferRecordRepository;
import nl.quintor.studybits.student.interfaces.StudentRepository;
import nl.quintor.studybits.student.interfaces.UniversityRepository;
import nl.quintor.studybits.student.model.ClaimOfferRecord;
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
    private ClaimOfferRecordRepository claimOfferRecordRepository;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Student register(@RequestParam(value = "username") String username, @RequestParam(value = "university") String uniName) throws Exception {
        if (studentRepository.getByUsername(username) != null)
            throw new IllegalArgumentException("Student with username exists already.");

        University university = universityRepository.getByName(uniName);
        if (university == null)
            throw new NotFoundException("University does not exist (yet).");

        Student student = new Student(username, university);
        studentRepository.save(student);

        return student;
    }

    @RequestMapping(value = "/{studentId}/claims", method = RequestMethod.GET)
    public List<ClaimOfferRecord> getClaims(@PathVariable Long studentId) throws Exception {
        Student student = studentRepository.getById(studentId);
        if (student == null)
            throw new NotFoundException("Student with username not found. Maybe register first.");

        return claimOfferRecordRepository.findAllByOwner(student);
    }

    @RequestMapping(value = "/{studentId}/claims/{claimId}", method = RequestMethod.GET)
    public ClaimOfferRecord getClaimById(@PathVariable Long studentId, @PathVariable Long claimId) throws Exception {
        ClaimOfferRecord claimOfferRecord = claimOfferRecordRepository.getById(claimId);
        if (claimOfferRecord.getOwner().getId().equals(studentId))
            throw new NotFoundException("Claim with id not found for student.");

        return claimOfferRecord;
    }
}
