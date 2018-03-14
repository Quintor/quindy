package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.services.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/university")
public class UniversityController {

    private UniversityService universityService;

    @PostMapping("/register")
    University register(@RequestParam("name") String name, @RequestParam("endpoint") String endpoint) {
        return universityService.createAndSave(name, endpoint);
    }

}
