package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.entities.MetaWallet;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.repositories.StudentRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StudentService {
    private StudentRepository studentRepository;
    private UniversityService universityService;
    private MetaWalletService metaWalletService;
    private ConnectionRecordService connectionRecordService;
    private IndyPool indyPool;
    private Mapper mapper;

    private Student toModel(Object student) {
        return mapper.map(student, Student.class);
    }

    @SneakyThrows
    public Student createAndSave(String username, String uniName) {
        if (studentRepository.existsByUserName(username))
            throw new IllegalArgumentException("StudentModel with username exists already.");

        University university = universityService.findByName(uniName)
                .orElseThrow(() -> new IllegalArgumentException("UniversityModel with id not found"));

        MetaWallet metaWallet = metaWalletService.create(username, uniName);
        try (IndyWallet indyWallet = metaWalletService.createIndyWalletFromMetaWallet(metaWallet)) {
            Prover prover = new Prover(username, indyPool, indyWallet, username);
            prover.init();
        }
        Student student = new Student(null, username, university, metaWallet);

        return studentRepository.save(student);
    }

    public Optional<Student> findById(Long studentId) {
        return studentRepository.findById(studentId)
                .map(this::toModel);
    }

    public List<Student> findAll() {
        return studentRepository.findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public Optional<Student> findByUserName(String name) {
        return studentRepository.findByUserName(name);
    }

    public Student findByNameOrElseThrow(String name) {
        return findByUserName(name)
                .orElseThrow(() -> new IllegalArgumentException("StudentModel with name not found."));
    }

    public void updateByObject(Student student) {
        Validate.isTrue(!studentRepository.existsById(student.getId()));
        studentRepository.save(student);
    }

    public void deleteByUserName(String studentUserName) {
        Student student = findByNameOrElseThrow(studentUserName);

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

    public void onboard(String studentUserName, String universityName) throws Exception {
        Student student = findByNameOrElseThrow(studentUserName);
        University university = universityService.findByNameOrElseThrow(universityName);

        URI uriBegin = universityService.buildOnboardingBeginUri(university, student);
        URI uriFinalize = universityService.buildOnboardingFinalizeUri(university, student);
        log.debug("Onboarding with uriBegin {}, uriEnd {}", uriBegin, uriFinalize);

        RestTemplate restTemplate = new RestTemplate();
        ConnectionRequest beginRequest = restTemplate.getForObject(uriBegin, ConnectionRequest.class);
        connectionRecordService.save(beginRequest, university, student);

        AnoncryptedMessage beginResponse = acceptConnectionRequest(student, beginRequest);
        ResponseEntity<Void> finalizeResponse = restTemplate.postForEntity(uriFinalize, beginResponse, Void.class);
        Validate.isTrue(finalizeResponse.getStatusCode().is2xxSuccessful());
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

