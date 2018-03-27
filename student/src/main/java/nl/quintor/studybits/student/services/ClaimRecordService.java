package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.model.*;
import nl.quintor.studybits.student.repositories.ClaimRecordRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimRecordService {
    private ClaimRecordRepository claimRecordRepository;
    private ConnectionRecordService connectionRecordService;
    private StudentService studentService;
    private Mapper mapper;

    private ClaimRecord toModel(Object claimRecord) {
        return mapper.map(claimRecord, ClaimRecord.class);
    }

    public ClaimRecord createAndSave(Long studentId, Claim claim) {
        Student student = studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));
        ClaimRecord claimRecord = new ClaimRecord(null, student, claim);

        return claimRecordRepository.save(claimRecord);
    }

    public Optional<ClaimRecord> findById(Long claimId) {
        return claimRecordRepository
                .findById(claimId)
                .map(this::toModel);
    }

    public List<ClaimRecord> findAllClaims(Long studentId) {
        Student owner = studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));

        return claimRecordRepository
                .findAllByOwner(owner)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void updateClaimById(Long claimId, ClaimRecord claimRecord) {
        if (!claimRecordRepository.existsById(claimId))
            throw new IllegalArgumentException("ClaimRecord with id not found.");

        // TODO: Add ownership check

        claimRecordRepository.save(claimRecord);
    }

    public void deleteAll() {
        claimRecordRepository.deleteAll();
    }

    public void fetchAndSaveNewClaimsForStudentId(Long studentId) throws Exception {
        Student student = studentService.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));
        Prover prover = studentService.getProverForStudent(student);

        getAllClaimInfo(student)
                .map(this::getClaimOfferForInfo)
                .map(AsyncUtil.wrapException(claimOffer -> getClaimForOffer(claimOffer, prover)))
                .filter(claim -> !claimRecordRepository.existsByClaim(claim))
                .forEach(claim -> createAndSave(studentId, claim));
    }

    private Stream<StudentClaimInfo> getAllClaimInfo(Student student) {
        return connectionRecordService.findAllConnections(student.getId())
                .stream()
                .map(ConnectionRecord::getUniversity)
                .map(university -> getAllStudentClaimInfo(university, student))
                .flatMap(List::stream);
    }

    private AuthEncryptedMessageModel getClaimOfferForInfo(StudentClaimInfo claimInfos) {
        return new RestTemplate().getForObject(claimInfos.getLink("self").toString(), AuthEncryptedMessageModel.class);
    }

    private Claim getClaimForOffer(AuthEncryptedMessageModel claimOffer, Prover prover) throws Exception {
        AuthcryptedMessage msg = this.getEncryptedClaimForOffer(claimOffer, prover);
        return decryptAuthcryptedMessage(msg, prover, Claim.class).get();
    }

    private List<StudentClaimInfo> getAllStudentClaimInfo(University university, Student student) {
        String path = UriComponentsBuilder
                .fromPath(university.getEndpoint())
                .path(university.getName())
                .path("/claims")
                .path(student.getUsername())
                .build()
                .toString();

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<List<StudentClaimInfo>>() {
        }).getBody();
    }

    @SneakyThrows
    private AuthcryptedMessage getEncryptedClaimForOffer(AuthEncryptedMessageModel messageModel, Prover prover) {
        AuthcryptedMessage authcryptedMessage = mapper.map(messageModel, AuthcryptedMessage.class);

        return decryptAuthcryptedMessage(authcryptedMessage, prover, ClaimOffer.class)
                .thenCompose(AsyncUtil.wrapException(prover::createClaimRequest))
                .thenApply(HttpEntity::new)
                .thenApply(entity -> new RestTemplate().postForEntity(messageModel.getLink("self")
                        .toString(), entity, AuthcryptedMessage.class))
                .thenApply(HttpEntity::getBody)
                .get();
    }

    @SneakyThrows
    private <T extends AuthCryptable> CompletableFuture<T> decryptAuthcryptedMessage(AuthcryptedMessage authMessage, Prover prover, Class<T> type) {
        return prover.authDecrypt(authMessage, type);
    }
}
