package nl.quintor.studybits.student.controller;

import nl.quintor.studybits.student.interfaces.UniversityRepository;
import nl.quintor.studybits.student.model.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/university")
public class UniversityController {
    @Autowired
    private UniversityRepository universityRepository;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public University register(@RequestParam("name") String name, @RequestParam("endpoint") String endpoint) {
        if (universityRepository.getByName(name) != null)
            throw new IllegalArgumentException("University with name exists already.");

        University university = new University(name, endpoint);
        universityRepository.save(university);

        return university;
    }

}
