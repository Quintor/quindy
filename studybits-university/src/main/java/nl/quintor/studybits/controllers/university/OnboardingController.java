package nl.quintor.studybits.controllers.university;

import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.services.OnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/university/{universityName}/onboarding")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;


    @GetMapping("/begin/{userName}")
    ConnectionRequest begin(@PathVariable("universityName") String universityName, @PathVariable("userName") String userName) throws Exception {
        return onboardingService.onboardBegin(universityName, userName);
    }

    @PostMapping("/finalize/{userName}")
    void finalize(@PathVariable("universityName") String universityName, @PathVariable("userName") String userName, @RequestBody AnoncryptedMessage anoncryptedMessage) {
        onboardingService.onboardFinalize(universityName, userName, anoncryptedMessage);
    }


}