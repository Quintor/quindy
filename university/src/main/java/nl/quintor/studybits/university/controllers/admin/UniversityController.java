package nl.quintor.studybits.university.controllers.admin;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.models.UniversityModel;
import nl.quintor.studybits.university.services.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{universityName}/admin/claims")
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping
    List<UniversityModel> findAll() {
        return universityService.findAll();
    }
}