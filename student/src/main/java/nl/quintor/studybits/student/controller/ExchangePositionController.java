package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.ExchangePositionRecord;
import nl.quintor.studybits.student.models.ExchangePositionModel;
import nl.quintor.studybits.student.services.ExchangePositionService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return exchangePositionService.getAllForStudentName(studentUserName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/new")
    void getAndSaveNewExchangePositions(@PathVariable String studentUserName) {
        exchangePositionService.getAndSaveNewExchangePositions(studentUserName);
    }
}

