package nl.quintor.studybits.services;

import lombok.SneakyThrows;
import nl.quintor.studybits.entities.IndyConnection;
import nl.quintor.studybits.entities.Student;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.repositories.StudentRepository;
import nl.quintor.studybits.repositories.UniversityRepository;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    private StudentRepository studentRepository;

    private Map<String, Issuer> issuers;

    @Autowired
    public OnboardingService(StudentRepository studentRepository, Issuer[] issuers) {
        this.studentRepository = studentRepository;
        this.issuers = Arrays
                .stream(issuers)
                .collect(Collectors.toMap(x -> x.getName().toLowerCase(), x -> x));
    }

    @SneakyThrows
    public ConnectionRequest onboardBegin(String universityName, String userName) throws Exception  {
        return withIssuerAndStudent(universityName, userName, (issuer, student) -> {
            Validate.isTrue(student.getConnection() == null, "Onboarding already completed!");
            return createConnectionRequest(issuer, student);
        });
    }

    @SneakyThrows
    public Boolean onboardFinalize(String universityName, String userName, AnoncryptedMessage anoncryptedMessage) {
        return withIssuerAndStudent(universityName, userName, (issuer, student) -> {
            Validate.isTrue(student.getConnection() == null, "Onboarding already completed!");
            String newcomerDid = processAnoncryptedMessage(issuer, anoncryptedMessage);
            student.setConnection(new IndyConnection(null, newcomerDid));
            studentRepository.saveAndFlush(student);
            return true;
        });
    }

    private <R> R withIssuerAndStudent(String universityName, String userName, BiFunction<Issuer, Student, R> func) {
        Issuer issuer = issuers.get(universityName.toLowerCase());
        Student student = studentRepository.findByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("Student not found!"));
        Validate.isTrue(student.university.getName().equalsIgnoreCase(universityName), "Onboarding failed because this is not a student of the university!");
        return func.apply(issuer, student);
    }

    @SneakyThrows
    private ConnectionRequest createConnectionRequest(Issuer issuer, Student student) {
        return issuer.createConnectionRequest(student.getUserName(), null).get();
    }

    @SneakyThrows
    private String processAnoncryptedMessage(Issuer issuer, AnoncryptedMessage anoncryptedMessage) {
        return issuer.anonDecrypt(anoncryptedMessage, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(issuer::acceptConnectionResponse)).get();
    }

}
