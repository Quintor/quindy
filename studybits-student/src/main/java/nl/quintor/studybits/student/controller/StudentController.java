package nl.quintor.studybits.student.controller;

import javassist.NotFoundException;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.student.interfaces.ClaimOfferRecordRepository;
import nl.quintor.studybits.student.interfaces.StudentRepository;
import nl.quintor.studybits.student.interfaces.UniversityRepository;
import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//        onboard(username, uniName);

        return student;
    }

    // TODO: Adapt this function to the University backend and check whether this needs to be an own endpoint at all.
    @RequestMapping(value = "/onboard", method = RequestMethod.POST)
    public ResponseEntity onboard(@RequestParam(value = "username") String username, @RequestParam(value = "university") String uniName) throws Exception {
        Student student = studentRepository.getByUsername(username);
        if (student == null)
            throw new NotFoundException("Student with username not found. Maybe register first.");

        University uni = universityRepository.getByName(uniName);
        if (uni == null)
            throw new NotFoundException("University with name not found.");

        RestTemplate requestInit = new RestTemplate();
        Map<String, Object> payloadInit = new HashMap<>();
        payloadInit.put("name", student.getUsername());

        ResponseEntity<ConnectionRequest> requestInitResponse = requestInit.getForEntity(uni.getEndpoint(), ConnectionRequest.class, payloadInit);
        ConnectionResponse responseInit = student.getProver().acceptConnectionRequest(requestInitResponse.getBody()).get();

        RestTemplate requestConfirmation = new RestTemplate();
        Map<String, Object> payloadConfirmation = new HashMap<>();
        payloadConfirmation.put("response", responseInit);

        // TODO: Handle appropriate return type from University API
        ResponseEntity<String> requestConfirmationResponse = requestConfirmation.getForEntity(uni.getEndpoint(), String.class, payloadConfirmation);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{studentId}/claims", method = RequestMethod.GET)
    public List<ClaimRecord> getClaims(@PathVariable Long studentId) throws Exception {
        Student student = studentRepository.getById(studentId);
        if (student == null)
            throw new NotFoundException("Student with username not found. Maybe register first.");

        return claimOfferRecordRepository.findAllByOwner(student);
    }

    @RequestMapping(value = "/{studentId}/claims/{claimId}", method = RequestMethod.GET)
    public ClaimRecord getClaimById(@PathVariable Long studentId, @PathVariable Long claimId) throws Exception {
        ClaimRecord claimRecord = claimOfferRecordRepository.getById(claimId);
        if (claimRecord.getOwner().getId().equals(studentId))
            throw new NotFoundException("Claim with id not found for student.");

        return claimRecord;
    }
}
