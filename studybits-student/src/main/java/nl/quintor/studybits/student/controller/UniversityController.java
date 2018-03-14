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
    University register(@RequestParam("name") String name, @RequestParam("endpoint") String endpoint) {
        return universityService.createAndSave(name, endpoint);
    }

    @GetMapping("/{uniId}")
    University findById(@PathVariable Long uniId) {
        return universityService
                .findById(uniId)
                .orElseThrow(() -> new IllegalArgumentException("University with id not found"));
    }

    @GetMapping("")
    List<University> findAll() {
        return universityService.findAll();
    }

    @PutMapping("/{uniId}")
    void updateById(@PathVariable Long uniId, @RequestParam("university") University university) {
        universityService.updateById(uniId, university);
    }

    @DeleteMapping("/{uniId}")
    void deleteById(@PathVariable Long uniId) {
        universityService.deleteById(uniId);
    }


}
