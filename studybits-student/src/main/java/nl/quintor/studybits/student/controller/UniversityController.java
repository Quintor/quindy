package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.interfaces.UniversityRepository;
import nl.quintor.studybits.student.model.University;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/university")
public class UniversityController {

    private UniversityRepository universityRepository;

    @PostMapping(value = "/register")
    University register(@RequestParam("name") String name, @RequestParam("endpoint") String endpoint) {
        if (universityRepository.getByName(name) != null)
            throw new IllegalArgumentException("University with name exists already.");

        University university = new University(null, name, endpoint);
        universityRepository.save(university);

        return university;
    }

}
