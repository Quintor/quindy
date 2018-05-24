package nl.quintor.studybits.university.controllers.admin;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.ExchangePositionRecord;
import nl.quintor.studybits.university.models.ExchangePositionModel;
import nl.quintor.studybits.university.services.ExchangePositionService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/admin/{userName}/positions")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AdminExchangePositionController {

    private final ExchangePositionService exchangePositionService;
    private final Mapper mapper;

    private ExchangePositionModel toModel(ExchangePositionRecord record) {
        return mapper.map(record, ExchangePositionModel.class);
    }

    @GetMapping
    List<ExchangePositionModel> getAllPositions() {
        return exchangePositionService
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @PostMapping
    ExchangePositionModel create(@RequestBody ExchangePositionModel position) {
        return toModel(this.exchangePositionService.create(position));
    }
}