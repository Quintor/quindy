package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.ExchangePositionRecord;
import nl.quintor.studybits.student.models.ExchangePositionModel;
import nl.quintor.studybits.student.services.ExchangePositionService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/{studentUserName}/positions")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangePositionController {
    private final ExchangePositionService exchangePositionService;
    private final Mapper mapper;

    private ExchangePositionModel toModel(ExchangePositionRecord record) {
        return mapper.map(record, ExchangePositionModel.class);
    }

    @GetMapping
    List<ExchangePositionModel> getAllForStudentName(@PathVariable String studentUserName) {
        return exchangePositionService.getAllExchangePositionsForStudentName(studentUserName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/new")
    void getAndSaveNewExchangePositions(@PathVariable String studentUserName) {
        exchangePositionService.getAndSaveNewExchangePositions(studentUserName);
    }

    @PostMapping
    void applyForExchangePosition(@PathVariable String studentUserName, @RequestBody ExchangePositionModel exchangePositionModel) throws Exception {
        exchangePositionService.applyForExchangePosition(studentUserName, exchangePositionModel);
    }
}

