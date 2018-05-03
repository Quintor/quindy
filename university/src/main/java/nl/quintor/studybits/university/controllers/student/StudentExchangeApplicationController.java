package nl.quintor.studybits.university.controllers.student;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.entities.ExchangeApplicationRecord;
import nl.quintor.studybits.university.models.ExchangeApplicationModel;
import nl.quintor.studybits.university.services.ExchangeApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/student/{userName}/applications")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StudentExchangeApplicationController {

    private final ExchangeApplicationService exchangeApplicationService;
    private final UserContext userContext;

    private ExchangeApplicationModel toModel(ExchangeApplicationRecord exchangeApplicationRecord) {
        return exchangeApplicationService.toModel(exchangeApplicationRecord);
    }

    @GetMapping
    List<ExchangeApplicationModel> getAllByUserName() {
        return exchangeApplicationService
                .findAllByUserName(userContext.currentUserName())
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}