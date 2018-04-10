package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.entities.MetaWallet;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.student.models.ProofRequestInfo;
import nl.quintor.studybits.student.models.StudentModel;
import nl.quintor.studybits.student.repositories.ClaimRepository;
import nl.quintor.studybits.student.repositories.StudentRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StudentService {
    private StudentRepository studentRepository;
    private UniversityService universityService;
    private MetaWalletService metaWalletService;
    private ClaimRepository claimRepository;
    private ConnectionRecordService connectionRecordService;
    private IndyPool indyPool;
    private Mapper mapper;

    private StudentModel toModel(Object student) {
        return mapper.map(student, StudentModel.class);
    }

    @SneakyThrows
    public Student createAndSave(String userName, String universityName) {
        if (studentRepository.existsByUserName(userName))
            throw new IllegalArgumentException("StudentModel with userName exists already.");

        University university = universityService.getByName(universityName);
        URI uriGetStudentInfo = universityService.buildGetStudentInfoUri(university, userName);
        StudentModel studentModel = new RestTemplate().getForObject(uriGetStudentInfo, StudentModel.class);

        MetaWallet metaWallet = metaWalletService.create(userName, universityName);
        try (IndyWallet indyWallet = metaWalletService.createIndyWalletFromMetaWallet(metaWallet)) {
            Prover prover = new Prover(userName, indyPool, indyWallet, userName);
            prover.init();
        }

        Student student = new Student(null, userName, studentModel.getFirstName(), studentModel.getLastName(), studentModel.getSsn(), university, metaWallet);
        return studentRepository.save(student);
    }

    public Optional<Student> findById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findByUserName(String name) {
        return studentRepository.findByUserName(name);
    }

    public Student getByUserName(String name) {
        return findByUserName(name)
                .orElseThrow(() -> new IllegalArgumentException("StudentModel with name not found."));
    }

    public void updateByObject(Student student) {
        Validate.isTrue(!studentRepository.existsById(student.getId()));
        studentRepository.save(student);
    }

    public void deleteByUserName(String studentUserName) {
        Student student = getByUserName(studentUserName);

        studentRepository.deleteById(student.getId());
    }

    public void deleteAll() throws Exception {
        log.debug("Deleting all students and wallets");
        List<Student> students = findAll();

        for (Student student : students) {
            metaWalletService.delete(student.getMetaWallet());
            studentRepository.deleteById(student.getId());

            log.debug("Deleted student {} with wallet {}", student.getId(), student.getMetaWallet().getId());
        }
    }

    public void connectWithUniversity(String studentUserName, String universityName) throws Exception {
        Student student = getByUserName(studentUserName);
        University university = universityService.getByName(universityName);

        Validate.isTrue(!student.getOriginUniversity().equals(university), "Cannot connect with origin university.");

        this.registerWithUniversity(student, university);
        this.onboard(student, university);
        this.proofIdentity(student, university);
    }

    private void registerWithUniversity(Student student, University university) {
        URI uriCreate = universityService.buildCreateStudentUri(university, student);

        ResponseEntity<StudentModel> response = new RestTemplate().postForEntity(uriCreate, toModel(student), StudentModel.class);
        Validate.isTrue(response.getStatusCode().is2xxSuccessful());
    }

    public void onboard(String studentUserName, String universityName) throws Exception {
        Student student = getByUserName(studentUserName);
        University university = universityService.getByName(universityName);

        this.onboard(student, university);
    }

    private void onboard(Student student, University university) throws Exception {
        URI uriBegin = universityService.buildOnboardingBeginUri(university, student);
        URI uriFinalize = universityService.buildOnboardingFinalizeUri(university, student);
        log.debug("Onboarding with uriBegin {}, uriEnd {}", uriBegin, uriFinalize);

        RestTemplate restTemplate = new RestTemplate();
        ConnectionRequest beginRequest = restTemplate.getForObject(uriBegin, ConnectionRequest.class);
        connectionRecordService.save(beginRequest, university, student);

        AnoncryptedMessage beginResponse = acceptConnectionRequest(student, beginRequest);
        ResponseEntity<Void> finalizeResponse = restTemplate.postForEntity(uriFinalize, beginResponse, Void.class);
        Validate.isTrue(finalizeResponse.getStatusCode().is2xxSuccessful(), "Could not get finalize onboarding with university");
    }

    private void proofIdentity(Student student, University university) throws Exception {
        ProofRequestInfo proofRequestInfo = this.getUserProofRequestInfo(student, university);
        AuthEncryptedMessageModel proof = this.getProofForProofRequest(student, proofRequestInfo);
        Boolean result = this.sendProofToUniversity(proof);

        Validate.isTrue(result, "Could not send UserProof to University Backend.");
    }

    private ProofRequestInfo getUserProofRequestInfo(Student student, University university) {
        return this.getAllProofRequests(student, university)
                .filter(proofRequestInfo -> proofRequestInfo.getName().equals("UserProof"))
                .sorted()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Could not find UserProof ClaimRequest for student."));
    }

    private Stream<ProofRequestInfo> getAllProofRequests(Student student, University university) {
        URI uriAllProofRequests = universityService.buildAllProofRequestsUri(university, student);
        return new RestTemplate()
                .exchange(uriAllProofRequests, HttpMethod.GET, null, new ParameterizedTypeReference<List<ProofRequestInfo>>() {})
                .getBody()
                .stream();
    }

    private AuthEncryptedMessageModel getProofForProofRequest(Student student, ProofRequestInfo requestInfo) throws Exception {
        AuthEncryptedMessageModel response = new RestTemplate()
                .getForObject(requestInfo.getLink("self").getHref(), AuthEncryptedMessageModel.class);

        Map<String, String> selfAttestedAttributes = new HashMap<>();
        selfAttestedAttributes.put("firstName", student.getFirstName());
        selfAttestedAttributes.put("lastName", student.getLastName());
        selfAttestedAttributes.put("ssn", student.getSsn());

        try (Prover prover = getProverForStudent(student)) {
            AuthEncryptedMessageModel model = prover.authDecrypt(mapper.map(response, AuthcryptedMessage.class), ProofRequest.class)
                    .thenCompose(AsyncUtil.wrapException(proofRequest -> prover.fulfillProofRequest(proofRequest, selfAttestedAttributes)))
                    .thenCompose(AsyncUtil.wrapException(prover::authEncrypt))
                    .thenApply(authcryptedMessage -> mapper.map(authcryptedMessage, AuthEncryptedMessageModel.class))
                    .get();

            requestInfo.getLinks().forEach(model::add);
            return model;
        }
    }

    private Boolean sendProofToUniversity(AuthEncryptedMessageModel proofModel) {
        return new RestTemplate().postForObject(proofModel.getLink("self").getHref(), proofModel, Boolean.class);
    }

    private AnoncryptedMessage acceptConnectionRequest(Student student, ConnectionRequest connectionRequest) throws Exception {
        try (Prover prover = getProverForStudent(student)) {
            return prover.acceptConnectionRequest(connectionRequest)
                    .thenCompose(AsyncUtil.wrapException(prover::anonEncrypt))
                    .get();
        }
    }

    public Prover getProverForStudent(Student student) throws Exception {
        IndyWallet wallet = metaWalletService.createIndyWalletFromMetaWallet(student.getMetaWallet());
        return new Prover(student.getUserName(), indyPool, wallet, student.getUserName());
    }
}

