package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.services.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/university")
public class UniversityController {

    private UniversityService universityService;

    @PostMapping("/register")
    University register(@RequestParam String name, @RequestParam String endpoint) {
        return universityService.createAndSave(name, endpoint);
    }

    @GetMapping("/{universityName}")
    University findById(@PathVariable String universityName) {
        return universityService.findByNameOrElseThrow(universityName);
    }

    @GetMapping
    List<University> findAll() {
        return universityService.findAll();
    }

    @PutMapping
    void updateByObject(@RequestBody University university) {
        universityService.updateByObject(university);
    }

    @DeleteMapping("/{universityName}")
    void deleteByName(@PathVariable String universityName) {
        universityService.deleteByName(universityName);
    }
}
