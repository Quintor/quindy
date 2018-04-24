package nl.quintor.studybits.university.controllers.admin;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.services.UniversityService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{universityName}/admin/{userName}/schemas")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SchemaController {

    private final UniversityService universityService;
    private final UserContext userContext;
    private final Mapper mapper;

    @GetMapping
    List<SchemaDefinition> getSchemaDefinitions() {
        return universityService.getSchemaDefinitions(userContext.currentUniversityName());
    }
}