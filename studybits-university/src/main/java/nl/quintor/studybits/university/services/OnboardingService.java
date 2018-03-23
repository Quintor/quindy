package nl.quintor.studybits.university.services;

import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.university.entities.IndyConnection;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.OnboardBegin;
import nl.quintor.studybits.university.models.OnboardFinalize;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
public class OnboardingService {

    @Autowired
    private Mapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IssuerService issuerService;

    @SneakyThrows
    public OnboardBegin onboardBegin(String universityName, String userName) {
        return withIssuerAndStudent(universityName, userName, this::createOnboardBegin);
    }

    @SneakyThrows
    public Boolean onboardFinalize(String universityName, String userName, OnboardFinalize onboardFinalize) {
        return withIssuerAndStudent(universityName, userName, (issuer, student) -> {
            String newcomerDid = processOnboardFinalize(issuer, onboardFinalize);
            student.setConnection(new IndyConnection(null, newcomerDid));
            userRepository.saveAndFlush(student);
            return true;
        });
    }

    private <R> R withIssuerAndStudent(String universityName, String userName, BiFunction<Issuer, User, R> func) {
        Issuer issuer = issuerService.getIssuer(universityName);
        User user = userRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName)
                .orElseThrow(() -> new IllegalArgumentException("UserIdentity not found!"));
        Validate.isTrue(user.getConnection() == null, "Onboarding already completed!");
        return func.apply(issuer, user);
    }

    @SneakyThrows
    private OnboardBegin createOnboardBegin(Issuer issuer, User user) {
        ConnectionRequest connect = issuer.createConnectionRequest(user.getUserName(), null).get();
        return mapper.map(connect, OnboardBegin.class);
    }

    @SneakyThrows
    private String processOnboardFinalize(Issuer issuer, OnboardFinalize onboardFinalize) {
        AnoncryptedMessage message = mapper.map(onboardFinalize, AnoncryptedMessage.class);
        return issuer.anonDecrypt(message, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(issuer::acceptConnectionResponse)).get();
    }
}