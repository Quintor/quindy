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
import nl.quintor.studybits.student.repositories.ClaimRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimService {
    private ClaimRepository claimRepository;
    private ConnectionRecordService connectionRecordService;
    private StudentService studentService;
    private Mapper mapper;

    private Claim toModel(Object claim) {
        return mapper.map(claim, Claim.class);
    }

    public Claim findById(Long claimId) {
        return claimRepository
                .findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim with id not found"));
    }

    public List<Claim> findAllClaims(Long studentId) {
        Student owner = studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));

        return claimRepository
                .findAllByOwner(owner)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void getAndSaveNewClaimsForStudentId(Long studentId) throws Exception {
        Student student = studentService.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));

        try (Prover prover = studentService.getProverForStudent(student)) {
            getAllStudentClaimInfo(student)
                    .map(this::getClaimOfferForStudentClaimInfo)
                    .map(AsyncUtil.wrapException(claimOffer -> getClaimForClaimOffer(claimOffer, prover, student)))
                    .filter(claim -> !claimRepository.existsById(claim.getId()))
                    .forEach(claim -> {
                        log.debug("Saving new claim {} to database...", claim);
                        claimRepository.save(claim);
                    });
        }
    }

    private Stream<StudentClaimInfo> getAllStudentClaimInfo(Student student) {
        return connectionRecordService.findAllConnections(student.getId())
                .stream()
                .map(ConnectionRecord::getUniversity)
                .flatMap(university -> getAllStudentClaimInfo(university, student));
    }

    private AuthEncryptedMessageModel getClaimOfferForStudentClaimInfo(StudentClaimInfo claimInfo) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(claimInfo.getLink("self")
                .getHref(), HttpMethod.GET, null, new ParameterizedTypeReference<AuthEncryptedMessageModel>() {
        }).getBody();
    }

    private Claim getClaimForClaimOffer(AuthEncryptedMessageModel claimOffer, Prover prover, Student owner) throws Exception {
        return this.getEncryptedClaimForOffer(claimOffer, prover)
                .thenCompose(msg -> decryptAuthcryptedMessage(msg, prover, Claim.class))
                .thenApply(this::toModel)
                .thenApply(claim -> {
                    claim.setOwner(owner);
                    return claim;
                })
                .get();
    }

    private Stream<StudentClaimInfo> getAllStudentClaimInfo(University university, Student student) {
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
        }).getBody().stream();
    }

    private CompletableFuture<AuthcryptedMessage> getEncryptedClaimForOffer(AuthEncryptedMessageModel messageModel, Prover prover) throws Exception {
        AuthcryptedMessage authcryptedMessage = mapper.map(messageModel, AuthcryptedMessage.class);

        return decryptAuthcryptedMessage(authcryptedMessage, prover, ClaimOffer.class)
                .thenCompose(AsyncUtil.wrapException(claimOffer -> {
                    log.debug("Creating ClaimRequest with claimOffer {}", claimOffer);
                    return prover.createClaimRequest(claimOffer);
                }))
                .thenApply(claimOffer -> {
                    log.debug("Retrieving AuthcryptedMessage from University with messageModel {} ", messageModel);
                    RestTemplate restTemplate = new RestTemplate();
                    return restTemplate.postForObject(messageModel.getLink("self")
                            .getHref(), claimOffer, AuthEncryptedMessageModel.class);
                })
                .thenApply(authEncryptedMessageModel -> mapper.map(authEncryptedMessageModel, AuthcryptedMessage.class));
    }

    @SneakyThrows
    private <T extends AuthCryptable> CompletableFuture<T> decryptAuthcryptedMessage(AuthcryptedMessage authMessage, Prover prover, Class<T> type) {
        log.debug("Decrypting AuthcryptedMessage {} with prover {} to class {}", authMessage, prover, type);
        return prover.authDecrypt(authMessage, type);
    }

    public void deleteAll() {
        claimRepository.deleteAll();
    }
}
