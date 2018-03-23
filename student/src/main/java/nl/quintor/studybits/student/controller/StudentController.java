package nl.quintor.studybits.student.controller;

import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.services.ClaimRecordService;
import nl.quintor.studybits.student.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClaimRecordService claimRecordService;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Student register(@RequestParam(value = "username") String username, @RequestParam(value = "university") String uniName) throws Exception {
        return studentService.createAndSave(username, uniName);
    }

//    // TODO: Adapt this function to the University backend and check whether this needs to be an own endpoint at all.
//    @RequestMapping(value = "/onboard", method = RequestMethod.POST)
//    public ResponseEntity onboard(@RequestParam(value = "username") String username, @RequestParam(value = "university") String uniName) throws Exception {
//        Student student = studentRepository.getByUsername(username);
//        if (student == null)
//            throw new NotFoundException("Student with username not found. Maybe register first.");
//
//        University uni = universityRepository.getByName(uniName);
//        if (uni == null)
//            throw new NotFoundException("University with name not found.");
//
//        RestTemplate requestInit = new RestTemplate();
//        Map<String, Object> payloadInit = new HashMap<>();
//        payloadInit.put("name", student.getUsername());
//
//        ResponseEntity<ConnectionRequest> requestInitResponse = requestInit.getForEntity(uni.getEndpoint(), ConnectionRequest.class, payloadInit);
//        ConnectionResponse responseInit = student.getProver().acceptConnectionRequest(requestInitResponse.getBody()).get();
//
//        RestTemplate requestConfirmation = new RestTemplate();
//        Map<String, Object> payloadConfirmation = new HashMap<>();
//        payloadConfirmation.put("response", responseInit);
//
//        // TODO: Handle appropriate return type from University API
//        ResponseEntity<String> requestConfirmationResponse = requestConfirmation.getForEntity(uni.getEndpoint(), String.class, payloadConfirmation);
//
//        return ResponseEntity.ok().build();
//    }

    @RequestMapping(value = "/{studentId}/claims", method = RequestMethod.GET)
    public List<ClaimRecord> getClaims(@PathVariable Long studentId) throws Exception {
        return claimRecordService.getAllClaimsForStudent(studentId);
    }

    @RequestMapping(value = "/{studentId}/claims/{claimId}", method = RequestMethod.GET)
    public ClaimRecord getClaimForStudent(@PathVariable Long studentId, @PathVariable Long claimId) throws Exception {
        return claimRecordService.getClaimForStudent(claimId, studentId);
    }
}
