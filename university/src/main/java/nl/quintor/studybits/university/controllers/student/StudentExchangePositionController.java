package nl.quintor.studybits.university.controllers.student;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.ExchangePositionRecord;
import nl.quintor.studybits.university.models.ExchangePositionModel;
import nl.quintor.studybits.university.services.ExchangePositionService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/student/{userName}/positions")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StudentExchangePositionController {

    private final ExchangePositionService exchangePositionService;
    private final Mapper mapper;

    private ExchangePositionModel toModel(ExchangePositionRecord exchangePositionRecord) {
        return mapper.map(exchangePositionRecord, ExchangePositionModel.class);
    }

    @GetMapping
    List<ExchangePositionModel> getAllPositions() {
        return exchangePositionService
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}