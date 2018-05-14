package nl.quintor.studybits.university.controllers.admin;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.models.UniversityModel;
import nl.quintor.studybits.university.services.UniversityService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/admin/{userName}")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UniversityController {

    private final UniversityService universityService;
    private final UserContext userContext;
    private final Mapper mapper;

    private UniversityModel toModel(University university) {
        return mapper.map(university, UniversityModel.class);
    }

    @PostMapping
    public void login() {
        if (!universityService.findUniversity(userContext.currentUniversityName()).isPresent()) {
            throw new IllegalArgumentException("University with name not found.");
        }
    }

    @GetMapping
    public List<UniversityModel> findAll() {
        return universityService
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}