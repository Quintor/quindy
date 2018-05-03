package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.models.ExchangeApplicationModel;
import nl.quintor.studybits.student.services.ExchangeApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/{studentUserName}/applications")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeApplicationController {

    private final ExchangeApplicationService exchangeApplicationService;

    @GetMapping
    List<ExchangeApplicationModel> getAllForStudentName(@PathVariable String studentUserName) {
        return exchangeApplicationService.getAllByStudentUserName(studentUserName)
                .stream()
                .map(exchangeApplicationService::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/new")
    void getAndSaveNewExchangeApplications(@PathVariable String studentUserName) {
        exchangeApplicationService.getAndSaveNewExchangeApplications(studentUserName);
    }

}

