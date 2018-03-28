package nl.quintor.studybits.university.controllers.student;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.models.OnboardBegin;
import nl.quintor.studybits.university.models.OnboardFinalize;
import nl.quintor.studybits.university.services.OnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{universityName}/student/{userName}/onboarding")
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping( "/begin" )
    OnboardBegin begin( @PathVariable String universityName, @PathVariable String userName ) {
        return onboardingService.onboardBegin(universityName, userName);
    }

    @PostMapping( "/finalize" )
    void finalize( @PathVariable String universityName, @PathVariable String userName, @RequestBody OnboardFinalize onboardFinalize ) {
        onboardingService.onboardFinalize(universityName, userName, onboardFinalize);
    }
}