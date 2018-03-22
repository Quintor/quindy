package nl.quintor.studybits.services;

import lombok.SneakyThrows;
import nl.quintor.studybits.entities.IndyConnection;
import nl.quintor.studybits.entities.Student;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.models.OnboardBegin;
import nl.quintor.studybits.models.OnboardFinalize;
import nl.quintor.studybits.repositories.StudentRepository;
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
    private StudentRepository studentRepository;

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
            studentRepository.saveAndFlush(student);
            return true;
        });
    }

    private <R> R withIssuerAndStudent(String universityName, String userName, BiFunction<Issuer, Student, R> func) {
        Issuer issuer = issuerService.getIssuer(universityName);
        Student student = studentRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName)
                .orElseThrow(() -> new IllegalArgumentException("Student not found!"));
        Validate.isTrue(student.getConnection() == null, "Onboarding already completed!");
        return func.apply(issuer, student);
    }

    @SneakyThrows
    private OnboardBegin createOnboardBegin(Issuer issuer, Student student) {
        ConnectionRequest connect = issuer.createConnectionRequest(student.getUserName(), null).get();
        return mapper.map(connect, OnboardBegin.class);
    }

    @SneakyThrows
    private String processOnboardFinalize(Issuer issuer, OnboardFinalize onboardFinalize) {
        AnoncryptedMessage message = mapper.map(onboardFinalize, AnoncryptedMessage.class);
        return issuer.anonDecrypt(message, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(issuer::acceptConnectionResponse)).get();
    }
}