package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.UniversityModel;
import nl.quintor.studybits.student.services.UniversityService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/university")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UniversityController {
    private UniversityService universityService;
    private Mapper mapper;

    private UniversityModel toModel(University university) {
        return mapper.map(university, UniversityModel.class);
    }

    @PostMapping("/register")
    UniversityModel register(@RequestParam String name, @RequestParam String endpoint) {
        return toModel(universityService.createAndSave(name, endpoint));
    }

    @GetMapping("/{universityName}")
    UniversityModel findById(@PathVariable String universityName) {
        return toModel(universityService.getByName(universityName));
    }

    @GetMapping
    List<UniversityModel> findAll() {
        return universityService
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @PutMapping
    void updateByObject(@RequestBody UniversityModel universityModel) {
        University university = mapper.map(universityModel, University.class);
        universityService.updateByObject(university);
    }

    @DeleteMapping("/{universityName}")
    void deleteByName(@PathVariable String universityName) {
        universityService.deleteByName(universityName);
    }
}
