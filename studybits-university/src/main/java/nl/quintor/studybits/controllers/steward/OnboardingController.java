package nl.quintor.studybits.controllers.steward;

import nl.quintor.studybits.Seeder;
import nl.quintor.studybits.indy.wrapper.TrustAnchor;
import nl.quintor.studybits.models.OnboardBeginRequest;
import nl.quintor.studybits.models.OnboardBeginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    @Autowired
    private TrustAnchor stewardTrustAnchor;


    @GetMapping("/begin")
    OnboardBeginResponse begin(OnboardBeginRequest request) throws Exception {

        OnboardBeginResponse response = new OnboardBeginResponse("1234", "nonce");
        return response;
    }

}
