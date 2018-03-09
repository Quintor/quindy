package nl.quintor.studybits.student;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@RestController
public class Student {
    @Getter
    private String email;
    private Prover prover;
    private IndyWallet indyWallet;

    @RequestMapping(value = "/onboard", method = RequestMethod.POST)
    public ResponseEntity<String> onboard(@RequestParam(value = "endpoint") String uniEndpoint) throws IOException, InterruptedException, ExecutionException, IndyException {
        ResponseEntity<ConnectionRequest> connectionRequest = new RestTemplate().getForEntity(uniEndpoint, ConnectionRequest.class, new HashMap<>().put("name", email));
        AnoncryptedMessage connectionResponse = prover.acceptConnectionRequest(connectionRequest.getBody()).get();

        return new RestTemplate().getForEntity(uniEndpoint, String.class, new HashMap<>().put("response", connectionResponse));
    }


}
