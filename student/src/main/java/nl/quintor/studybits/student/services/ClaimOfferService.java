package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.student.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor( onConstructor = @__( @Autowired ) )
public class ClaimOfferService {

    private ConnectionRecordService connectionRecordService;
    private IndyPool indyPool;
    private MetaWalletService metaWalletService;

    public List<Claim> fetchAllForStudent( Student student ) {
        List<StudentClaimInfo> claimInfos = getAllClaimInfo(student);

        IndyWallet wallet = metaWalletService.createIndyWalletFromMetaWallet(student.getMetaWallet());
        Prover prover = new Prover(student.getUsername(), indyPool, wallet);
        List<ClaimOffer> claimOffers = getAllClaimOffers(claimInfos, prover);

        return getAllClaims(claimOffers, prover);
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
        return restTemplate.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<List<StudentClaimInfo>>() {})
                           .getBody();
    }

    private List<ClaimOffer> getAllClaimOffers( List<StudentClaimInfo> claimInfos, Prover prover ) {
        List<AuthcryptedMessage> authMessages = claimInfos.stream()
                                                          .map(this::getEncryptedClaimOffer)
                                                          .collect(Collectors.toList());
        return authMessages.stream()
                           .map(msg -> decryptClaimOffer(msg, prover))
                           .collect(Collectors.toList());
    }

    private AuthcryptedMessage getEncryptedClaimOffer( StudentClaimInfo studentClaimInfo ) {
        return new RestTemplate().getForObject(studentClaimInfo.getLink("self")
                                                               .toString(), AuthcryptedMessage.class);
    }

    @SneakyThrows
    private ClaimOffer decryptClaimOffer( AuthcryptedMessage authMessage, Prover prover ) {
        return prover.authDecrypt(authMessage, ClaimOffer.class)
                     .get();
    }

    private List<Claim> getAllClaims( List<ClaimOffer> claimOffers, Prover prover ) {
        throw new NotImplementedException();
    }

}
