package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.entities.ConnectionRecord;
import nl.quintor.studybits.student.entities.MetaWallet;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.StudentModel;
import nl.quintor.studybits.student.repositories.StudentRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
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
    private ProofRequestService proofRequestService;
    private StudentProverService studentProverService;
    private ConnectionService connectionService;
    private Mapper mapper;

    private StudentModel toModel(Object student) {
        return mapper.map(student, StudentModel.class);
    }

    @Transactional
    public Student createAndOnboard(String userName, String universityName) throws Exception {
        if (studentRepository.existsByUserNameIgnoreCase(userName))
            throw new IllegalArgumentException("StudentModel with userName exists already.");

        University university = universityService.getByName(universityName);
        StudentModel studentModel = this.getStudentInfo(userName, university);
        MetaWallet metaWallet = metaWalletService.createAndInit(userName, universityName);

        Student student = new Student(null, userName, studentModel.getFirstName(), studentModel.getLastName(), studentModel.getSsn(), university, metaWallet);
        metaWallet.setStudent(student);
        studentRepository.save(student);

        this.onboard(student, university);

        return student;
    }

    private StudentModel getStudentInfo(String userName, University university) {
        URI uriGetStudentInfo = universityService.buildGetStudentInfoUri(university, userName);
        return new RestTemplate().getForObject(uriGetStudentInfo, StudentModel.class);
    }

    public Optional<Student> findById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findByUserName(String name) {
        return studentRepository.findByUserNameIgnoreCase(name);
    }

    public List<University> findAllConnectedUniversities(String userName) {
        return connectionService
                .findAllByStudentUserName(userName)
                .stream()
                .map(ConnectionRecord::getUniversity)
                .collect(Collectors.toList());
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

    public void connectWithUniversity(String studentUserName, String universityName) throws Exception {
        Student student = getByUserName(studentUserName);
        University university = universityService.getByName(universityName);

        Validate.isTrue(!student.getOriginUniversity().equals(university), "Cannot connect with origin university.");

        this.registerWithUniversity(student, university);
        this.onboard(student, university);
        proofRequestService.getAndSaveNewProofRequests(student, university);
    }

    private void registerWithUniversity(Student student, University university) {
        URI uriCreate = universityService.buildCreateStudentUri(university, student);

        log.debug("Connecting to university with path {}", uriCreate);

        ResponseEntity<StudentModel> response = new RestTemplate().postForEntity(uriCreate, toModel(student), StudentModel.class);
        Validate.isTrue(response.getStatusCode().is2xxSuccessful());
    }

    private void onboard(Student student, University university) throws Exception {
        URI uriBegin = universityService.buildOnboardingBeginUri(university, student);
        URI uriFinalize = universityService.buildOnboardingFinalizeUri(university, student);
        log.debug("Onboarding with uriBegin {}, uriEnd {}", uriBegin, uriFinalize);

        RestTemplate restTemplate = new RestTemplate();
        ConnectionRequest beginRequest = restTemplate.getForObject(uriBegin, ConnectionRequest.class);
        connectionService.save(beginRequest, university, student);

        AnoncryptedMessage beginResponse = acceptConnectionRequest(student, beginRequest);
        ResponseEntity<Void> finalizeResponse = restTemplate.postForEntity(uriFinalize, beginResponse, Void.class);
        Validate.isTrue(finalizeResponse.getStatusCode().is2xxSuccessful(), "Could not get finalize onboarding with university");
    }

    private AnoncryptedMessage acceptConnectionRequest(Student student, ConnectionRequest connectionRequest) throws Exception {
        return studentProverService.withProverForStudent(student, prover -> {
            try {
                return prover
                        .acceptConnectionRequest(connectionRequest)
                        .thenCompose(AsyncUtil.wrapException(prover::anonEncrypt))
                        .get();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }
}

