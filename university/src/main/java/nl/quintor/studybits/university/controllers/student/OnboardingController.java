package nl.quintor.studybits.university.controllers.student;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.models.OnboardBegin;
import nl.quintor.studybits.university.models.OnboardFinalize;
import nl.quintor.studybits.university.services.OnboardingService;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{universityName}/student/{userName}/onboarding")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class OnboardingController {

    private final UserContext userContext;
    private final OnboardingService onboardingService;
    private final Mapper mapper;

    @GetMapping("/begin")
    OnboardBegin begin() {
        Validate.notNull(userContext.currentUserId(), "Unknown user.");
        ConnectionRequest connectionRequest = onboardingService
                .onboardBegin(userContext.currentUniversityName(), userContext.currentUserName());
        return mapper.map(connectionRequest, OnboardBegin.class);
    }

    @PostMapping("/finalize")
    void finalize(@RequestBody OnboardFinalize onboardFinalize) {
        AnoncryptedMessage anoncryptedMessage = mapper.map(onboardFinalize, AnoncryptedMessage.class);
        onboardingService.onboardFinalize(userContext.currentUniversityName(), userContext.currentUserId(), anoncryptedMessage);
    }
}