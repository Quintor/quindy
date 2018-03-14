package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    @PostMapping("/register")
    Student register(@RequestParam(value = "username") String username, @RequestParam(value = "university") String uniName) {
        return studentService.createAndSave(username, uniName);
    }

    @GetMapping("/{studentId}")
    Student findById(@PathVariable Long studentId) {
        return studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));
    }

    @GetMapping("")
    List<Student> findAll() {
        return studentService.findAll();
    }

    @PutMapping("/{studentId}")
    void updateById(@PathVariable Long studentId, @RequestParam("student") Student student) {
        studentService.updateById(studentId, student);
    }

    @DeleteMapping("/{studentId}")
    void deleteById(@PathVariable Long studentId) {
        studentService.deleteById(studentId);
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


}
