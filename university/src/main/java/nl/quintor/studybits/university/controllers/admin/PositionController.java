package nl.quintor.studybits.university.controllers.admin;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.entities.PositionRecord;
import nl.quintor.studybits.university.models.PositionModel;
import nl.quintor.studybits.university.services.PositionService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/admin/{userName}/positions")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PositionController {

    private final PositionService positionService;
    private final UserContext userContext;
    private final Mapper mapper;

    private PositionModel toModel(PositionRecord positionRecord) {
        return mapper.map(positionRecord, PositionModel.class);
    }

    @GetMapping
    List<PositionModel> getAllPositions() {
        return positionService
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @PostMapping
    void create(@RequestBody PositionModel position) {
        this.positionService.create(position);
    }
}