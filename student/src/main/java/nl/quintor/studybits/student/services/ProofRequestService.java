package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.entities.ConnectionRecord;
import nl.quintor.studybits.student.entities.ProofRequestRecord;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.student.models.ProofRequestInfo;
import nl.quintor.studybits.student.models.ProofRequestModel;
import nl.quintor.studybits.student.repositories.ProofRequestRecordRepository;
import nl.quintor.studybits.student.repositories.StudentRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProofRequestService {

    private ProofRequestRecordRepository proofRequestRecordRepository;
    private StudentRepository studentRepository;
    private UniversityService universityService;
    private ConnectionRecordService connectionRecordService;
    private StudentProverService studentProverService;
    private Mapper mapper;

    public void getAndSaveNewProofRequests(String studentUserName) {
        Student student = studentRepository.findByUserName(studentUserName)
                .orElseThrow(() -> new IllegalArgumentException("Could not find student with userName."));

        connectionRecordService
                .findAllByStudentUserName(studentUserName)
                .stream()
                .map(ConnectionRecord::getUniversity)
                .forEach(university -> this.getAndSaveNewProofRequests(student, university));
    }

    public void getAndSaveNewProofRequests(Student student, University university) {
        this.getAllProofRequests(student, university)
                .map(proofRequestInfo -> this.toRecordFromInfo(student, university, proofRequestInfo))
                .filter(this::notExisting)
                .forEach(proofRequestRecordRepository::save);
    }

    public void fulfillProofRequest(ProofRequestRecord proofRequestRecord) throws Exception {
        ProofRequestInfo proofRequestInfo = this.getInfoFromRecord(proofRequestRecord);

        studentProverService.withProverForStudent(proofRequestRecord.getStudent(), prover -> {
            try {
                AuthEncryptedMessageModel proof = this.getProofForProofRequest(proofRequestRecord.getStudent(), prover, proofRequestInfo);
                Boolean result = this.sendProofToUniversity(proof);

                if (result) {
                    proofRequestRecord.setIsReviewed(true);
                    this.proofRequestRecordRepository.save(proofRequestRecord);
                } else {
                    throw new IllegalStateException("Could not fulfill proof request. University returned failure.");
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private Stream<ProofRequestInfo> getAllProofRequests(Student student, University university) {
        URI uriAllProofRequests = universityService.buildAllProofRequestsUri(university, student);
        return new RestTemplate()
                .exchange(uriAllProofRequests, HttpMethod.GET, null, new ParameterizedTypeReference<List<ProofRequestInfo>>() {})
                .getBody()
                .stream();
    }

    private AuthEncryptedMessageModel getProofForProofRequest(Student student, Prover prover, ProofRequestInfo requestInfo) throws Exception {
        AuthEncryptedMessageModel response = new RestTemplate()
                .getForObject(requestInfo.getLink("self").getHref(), AuthEncryptedMessageModel.class);

        AuthEncryptedMessageModel model = prover.authDecrypt(mapper.map(response, AuthcryptedMessage.class), ProofRequest.class)
                .thenCompose(AsyncUtil.wrapException(proofRequest -> prover.fulfillProofRequest(proofRequest, new HashMap<>())))
                .thenCompose(AsyncUtil.wrapException(prover::authEncrypt))
                .thenApply(authcryptedMessage -> mapper.map(authcryptedMessage, AuthEncryptedMessageModel.class))
                .get();

        requestInfo.getLinks().forEach(model::add);
        return model;
    }

    private Boolean sendProofToUniversity(AuthEncryptedMessageModel proofModel) {
        return new RestTemplate().postForObject(proofModel.getLink("self").getHref(), proofModel, Boolean.class);
    }

    public List<ProofRequestRecord> findAllByStudentUserName(String studentUserName) {
        return proofRequestRecordRepository.findAllByStudentUserName(studentUserName);
    }

    private boolean notExisting(ProofRequestRecord proofRequestRecord) {
        return !proofRequestRecordRepository
                .existsByStudentAndNameAndVersion(proofRequestRecord.getStudent(), proofRequestRecord.getName(), proofRequestRecord.getVersion());
    }

    public ProofRequestRecord getRecordFromModel(ProofRequestModel proofRequestModel) {
        return proofRequestRecordRepository
                .findByStudentUserNameAndNameAndVersion(proofRequestModel.getStudentUserName(), proofRequestModel.getName(), proofRequestModel
                        .getVersion())
                .orElseThrow(() -> new IllegalArgumentException("Could not " + "find ProofRequestRecord for ProofRequestModel."));
    }

    private ProofRequestRecord toRecordFromInfo(Student student, University university, ProofRequestInfo proofRequestInfo) {
        ProofRequestRecord proofRequestRecord = new ProofRequestRecord();

        proofRequestRecord.setStudent(student);
        proofRequestRecord.setUniversity(university);
        proofRequestRecord.setLink(proofRequestInfo.getLink("self").getHref());
        proofRequestRecord.setProofId(proofRequestInfo.getProofId());
        proofRequestRecord.setName(proofRequestInfo.getName());
        proofRequestRecord.setVersion(proofRequestInfo.getVersion());
        proofRequestRecord.setAttributes(proofRequestInfo.getAttributes());
        proofRequestRecord.setIsReviewed(false);

        return proofRequestRecord;
    }

    public void deleteAll() {
        proofRequestRecordRepository.deleteAll();
    }

    private ProofRequestInfo getInfoFromRecord(ProofRequestRecord proofRequestRecord) {
        ProofRequestInfo proofRequestInfo = mapper.map(proofRequestRecord, ProofRequestInfo.class);
        proofRequestInfo.add(new Link(proofRequestRecord.getLink(), "self"));
        return proofRequestInfo;
    }
}
