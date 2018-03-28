package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    public void getAndSaveNewClaimsForStudentId(Long studentId) throws Exception {
        Student student = studentService.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));

        try (Prover prover = studentService.getProverForStudent(student)) {
            getAllClaimInfo(student)
                    .map(this::getClaimOfferForInfo)
                    .map(AsyncUtil.wrapException(claimOffer -> getClaimForOffer(claimOffer, prover)))
                    .filter(claim -> !claimRecordRepository.existsByClaim(claim))
                    .forEach(claim -> createAndSave(studentId, claim));
        }
    }

    private Stream<StudentClaimInfo> getAllClaimInfo(Student student) {
        return connectionRecordService.findAllConnections(student.getId())
                .stream()
                .map(ConnectionRecord::getUniversity)
                .map(university -> getAllStudentClaimInfo(university, student))
                .flatMap(List::stream);
    }

    private AuthEncryptedMessageModel getClaimOfferForInfo(StudentClaimInfo claimInfo) {
        return new RestTemplate().getForObject(claimInfo.getLink("self").getHref(), AuthEncryptedMessageModel.class);
    }

    private Claim getClaimForOffer(AuthEncryptedMessageModel claimOffer, Prover prover) throws Exception {
        AuthcryptedMessage msg = this.getEncryptedClaimForOffer(claimOffer, prover);
        return decryptAuthcryptedMessage(msg, prover, Claim.class).get();
    }

    private List<StudentClaimInfo> getAllStudentClaimInfo(University university, Student student) {
        String path = UriComponentsBuilder
                .fromHttpUrl(university.getEndpoint())
                .path(university.getName())
                .path("/student/")
                .path(student.getUsername())
                .path("/claims")
                .build()
                .toString();

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<List<StudentClaimInfo>>() {
        }).getBody();
    }

    private AuthcryptedMessage getEncryptedClaimForOffer(AuthEncryptedMessageModel messageModel, Prover prover) throws Exception {
        AuthcryptedMessage authcryptedMessage = mapper.map(messageModel, AuthcryptedMessage.class);

        return decryptAuthcryptedMessage(authcryptedMessage, prover, ClaimOffer.class)
                .thenCompose(AsyncUtil.wrapException(claimOffer -> {
                    log.debug("Creating ClaimRequest with claimOffer {}", claimOffer);
                    return prover.createClaimRequest(claimOffer);
                }))
                .thenApply(HttpEntity::new)
                .thenApply(entity -> {
                    log.debug("Retrieving AuthcryptedMessage from University with link {} and entity {}", messageModel.getLink("self")
                            .getHref(), entity);
                    return new RestTemplate().postForEntity(messageModel.getLink("self")
                            .getHref(), entity, AuthcryptedMessage.class);
                })
                .thenApply(HttpEntity::getBody)
                .get();
    }

    @SneakyThrows
    private <T extends AuthCryptable> CompletableFuture<T> decryptAuthcryptedMessage(AuthcryptedMessage authMessage, Prover prover, Class<T> type) {
        log.debug("Decrypting AuthcryptedMessage {} with prover {} to class {}", authMessage, prover, type);
        return prover.authDecrypt(authMessage, type);
    }
}
