package nl.quintor.studybits.services;

import com.sun.org.apache.xalan.internal.lib.ExsltBase;
import lombok.SneakyThrows;
import nl.quintor.studybits.entities.University;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.TrustAnchor;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.models.OnboardBeginResponse;
import nl.quintor.studybits.repositories.StudentRepository;
import nl.quintor.studybits.repositories.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StewardService {

    @Autowired
    private TrustAnchor stewardTrustAnchor;

    @Autowired
    private UniversityRepository universityRepository;

    public StewardService() {

    }

    @SneakyThrows
    public OnboardBeginResponse onboardBegin(String name) throws Exception {
        Optional<University> university = universityRepository.findByName(name);
        university.map(u -> {

                ConnectionRequest request = stewardTrustAnchor.createConnectionRequest(name, "TRUST_ANCHOR").get();
            stewardTrustAnchor.acceptConnectionResponse()
            }
        ).orElseThrow(new IllegalArgumentException("University not found!"));


    }

}
