package nl.quintor.studybits.student;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@RestController
public class Student {
    @Getter
    private String email;
    private Prover prover;
    private IndyWallet indyWallet;

    @RequestMapping(value = "/onboard", method = RequestMethod.POST)
    public ResponseEntity<String> onboard(@RequestParam(value = "endpoint") String uniEndpoint) throws Exception {
        RestTemplate requestInit = new RestTemplate();
        Map<String, Object> payloadInit = new HashMap<>();
        payloadInit.put("name", email);

        ResponseEntity<ConnectionRequest> requestInitResponse = requestInit.getForEntity(uniEndpoint, ConnectionRequest.class, payloadInit);
        AnoncryptedMessage responseInit = prover.acceptConnectionRequest(requestInitResponse.getBody())
                .thenCompose(AsyncUtil.wrapException(prover::anoncrypt)).get();

        RestTemplate requestConfirmation = new RestTemplate();
        Map<String, Object> payloadConfirmation = new HashMap<>();
        payloadConfirmation.put("response", responseInit);

        return new RestTemplate().getForEntity(uniEndpoint, String.class, payloadConfirmation);
    }


}
