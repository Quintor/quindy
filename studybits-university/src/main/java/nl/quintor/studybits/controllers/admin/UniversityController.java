package nl.quintor.studybits.controllers.admin;

import nl.quintor.studybits.models.University;
import nl.quintor.studybits.services.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{universityName}/admin/claims")
public class UniversityController {

    @Autowired
    private UniversityService universityService;

    @GetMapping("")
    List<University> findAll() {
        return universityService.findAll();
    }
}