package nl.quintor.studybits.controllers;

import nl.quintor.studybits.models.OnboardBegin;
import nl.quintor.studybits.models.OnboardFinalize;
import nl.quintor.studybits.services.OnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{universityName}/onboarding")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;


    @GetMapping("/begin/{userName}")
    OnboardBegin begin(@PathVariable String universityName, @PathVariable String userName) throws Exception {
        return onboardingService.onboardBegin(universityName, userName);
    }

    @PostMapping("finalize/{userName}")
    void finalize(@PathVariable String universityName, @PathVariable String userName, @RequestBody OnboardFinalize onboardFinalize) {
        onboardingService.onboardFinalize(universityName, userName, onboardFinalize);
    }


}