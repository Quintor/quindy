package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.dto.ClaimRequest;
import nl.quintor.studybits.student.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor( onConstructor = @__( @Autowired ) )
public class ClaimOfferService {

    private ConnectionRecordService connectionRecordService;
    private StudentService studentService;

    public List<Claim> getAllForStudent( Student student ) throws Exception {
        try (Prover prover = studentService.getProverForStudent(student)) {

            List<StudentClaimInfo> claimInfos = getAllClaimInfo(student);
            List<ClaimOffer> claimOffers = getAllClaimOffers(claimInfos, prover);

            return getAllClaims(claimOffers, prover);
        }
    }

    private List<StudentClaimInfo> getAllClaimInfo( Student student ) {
        List<University> universities = connectionRecordService.findAllConnections(student.getId())
                                                               .stream()
                                                               .map(ConnectionRecord::getUniversity)
                                                               .collect(Collectors.toList());
        return universities.stream()
                           .map(university -> getAllStudentClaimInfo(university, student))
                           .flatMap(List::stream)
                           .collect(Collectors.toList());
    }

    @SneakyThrows
    private List<StudentClaimInfo> getAllStudentClaimInfo( University university, Student student ) {
        String path = String.format("%s/%s/claims/%s", university.getEndpoint(), university.getName(), student.getUsername());

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<List<StudentClaimInfo>>() {}).getBody();
    }

    private List<ClaimOffer> getAllClaimOffers( List<StudentClaimInfo> claimInfos, Prover prover ) {
        return claimInfos.stream()
                         .map(this::getEncryptedClaimOffer)
                         .map(msg -> decryptAuthcryptedMessage(msg, prover, ClaimOffer.class))
                         .collect(Collectors.toList());
    }

    private AuthcryptedMessage getEncryptedClaimOffer( StudentClaimInfo studentClaimInfo ) {
        return new RestTemplate().getForObject(studentClaimInfo.getLink("self").toString(), AuthcryptedMessage.class);
    }

    private List<Claim> getAllClaims( List<ClaimOffer> claimOffers, Prover prover ) {
        return claimOffers.stream()
                          .map(claimOffer -> this.getEncryptedClaimForOffer(claimOffer, prover))
                          .map(msg -> decryptAuthcryptedMessage(msg, prover, Claim.class))
                          .collect(Collectors.toList());
    }

    @SneakyThrows
    private AuthcryptedMessage getEncryptedClaimForOffer( ClaimOffer claimOffer, Prover prover ) {
        ClaimRequest claimRequest = prover.createClaimRequest(claimOffer).get();
        HttpEntity<ClaimRequest> entity = new HttpEntity<>(claimRequest);

        return null;

//        return new RestTemplate().postForEntity(claimOffer.getLink("self").toString(), entity, AuthcryptedMessage.class).getBody();
    }

    @SneakyThrows
    private <T extends AuthCryptable> T decryptAuthcryptedMessage( AuthcryptedMessage authMessage, Prover prover, Class<T> type ) {
        return prover.authDecrypt(authMessage, type).get();
    }
}
