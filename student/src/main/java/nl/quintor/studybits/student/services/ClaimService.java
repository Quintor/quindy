package nl.quintor.studybits.student.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.entities.*;
import nl.quintor.studybits.student.model.AuthEncryptedMessageModel;
import nl.quintor.studybits.student.model.StudentClaimInfoModel;
import nl.quintor.studybits.student.repositories.ClaimRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ClaimService {
    private ClaimRepository claimRepository;
    private ConnectionRecordService connectionRecordService;
    private SchemaKeyService schemaKeyService;
    private StudentService studentService;
    private Mapper mapper;

    @Value("${database.claim.hash.function}")
    private String hashFunction;

    public ClaimService(ClaimRepository claimRepository, ConnectionRecordService connectionRecordService, SchemaKeyService schemaKeyService, StudentService studentService, Mapper mapper) {
        this.claimRepository = claimRepository;
        this.connectionRecordService = connectionRecordService;
        this.schemaKeyService = schemaKeyService;
        this.studentService = studentService;
        this.mapper = mapper;
    }

    public void getAndSaveNewClaimsForOwnerUserName(String studentUserName) throws Exception {
        Student student = studentService.findByNameOrElseThrow(studentUserName);

        try (Prover prover = studentService.getProverForStudent(student)) {
            getAllStudentClaimInfo(student)
                    .map(this::getClaimOfferForStudentClaimInfo)
                    .map(AsyncUtil.wrapException(claimOffer -> getClaimForClaimOffer(claimOffer, prover, student)))
                    .filter(claim -> !claimRepository.existsBySignature(claim.getSignature()))
                    .forEach(claim -> {
                        log.debug("Saving new claimModel {} to database...", claim);
                        claimRepository.save(claim);
                    });
        }
    }

    private Stream<StudentClaimInfoModel> getAllStudentClaimInfo(Student student) {
        return connectionRecordService.findAllByStudentUserName(student.getUserName())
                .stream()
                .map(ConnectionRecord::getUniversity)
                .flatMap(university -> getAllStudentClaimInfo(university, student));
    }

    private AuthEncryptedMessageModel getClaimOfferForStudentClaimInfo(StudentClaimInfoModel claimInfo) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(claimInfo.getLink("self")
                .getHref(), HttpMethod.GET, null, new ParameterizedTypeReference<AuthEncryptedMessageModel>() {
        }).getBody();
    }

    private Claim getClaimForClaimOffer(AuthEncryptedMessageModel claimOffer, Prover prover, Student owner) throws Exception {
        AuthcryptedMessage authcryptedMessage = getEncryptedClaimForOffer(claimOffer, prover);
        Claim claim = decryptAuthcryptedMessage(authcryptedMessage, prover, nl.quintor.studybits.indy.wrapper.dto.Claim.class)
                .thenApply(wrapperClaim -> mapper.map(wrapperClaim, Claim.class))
                .get();
        claim.setOwner(owner);
        claim.setHashId(hashClaim(claim));
        return claim;
    }

    private String hashClaim(Claim claim) {
        return DigestUtils.sha256Hex(claim.getValues());
    }

    private Stream<StudentClaimInfoModel> getAllStudentClaimInfo(University university, Student student) {
        URI path = UriComponentsBuilder
                .fromHttpUrl(university.getEndpoint())
                .path(university.getName())
                .path("/student/")
                .path(student.getUserName())
                .path("/claims")
                .build().toUri();

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<List<StudentClaimInfoModel>>() {
        }).getBody().stream();
    }

    private AuthcryptedMessage getEncryptedClaimForOffer(AuthEncryptedMessageModel encryptedClaimOffer, Prover prover) throws Exception {
        AuthcryptedMessage claimOffer = mapper.map(encryptedClaimOffer, AuthcryptedMessage.class);
        AuthEncryptedMessageModel encryptedClaimRequest = getEncryptedClaimRequestForClaimOffer(claimOffer, prover);

        log.debug("Retrieving ClaimModel from UniversityModel with claimOffer {} ", claimOffer);
        RestTemplate restTemplate = new RestTemplate();
        AuthEncryptedMessageModel response = restTemplate.postForObject(encryptedClaimOffer.getLink("self")
                .getHref(), encryptedClaimRequest, AuthEncryptedMessageModel.class);
        return mapper.map(response, AuthcryptedMessage.class);
    }

    private AuthEncryptedMessageModel getEncryptedClaimRequestForClaimOffer(AuthcryptedMessage encryptedClaimOffer, Prover prover) throws ExecutionException, InterruptedException {
        return decryptAuthcryptedMessage(encryptedClaimOffer, prover, ClaimOffer.class)
                .thenCompose(AsyncUtil.wrapException(claimOffer -> {
                    log.debug("Creating ClaimRequest with claimOffer {}", claimOffer);
                    return prover.createClaimRequest(claimOffer);
                }))
                .thenCompose(AsyncUtil.wrapException(claimRequest -> {
                    log.debug("AuthEncrypting ClaimRequest {} with Prover.", claimRequest);
                    return prover.authEncrypt(claimRequest);
                }))
                .thenApply(encryptedClaimRequest -> mapper.map(encryptedClaimRequest, AuthEncryptedMessageModel.class))
                .get();
    }

    @SneakyThrows
    private <T extends AuthCryptable> CompletableFuture<T> decryptAuthcryptedMessage(AuthcryptedMessage authMessage, Prover prover, Class<T> type) {
        log.debug("Decrypting AuthcryptedMessage {} with prover {} to class {}", authMessage, prover, type);
        return prover.authDecrypt(authMessage, type);
    }

    public Claim findByIdOrElseThrow(Long claimId) {
        return claimRepository
                .findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("ClaimModel with id not found"));
    }

    public List<Claim> findAllByOwnerUserName(String studentUserName) {
        Student owner = studentService.findByNameOrElseThrow(studentUserName);
        return claimRepository.findAllByOwner(owner);
    }

    public void deleteAll() {
        claimRepository.deleteAll();
    }

    public List<Claim> findByOwnerUserNameAndSchemaKeyName(String studentUserName, String schemaName) {
        Student student = studentService.findByNameOrElseThrow(studentUserName);
        SchemaKey schemaKey = schemaKeyService.findByNameOrElseThrow(schemaName);

        return claimRepository
                .findAllBySchemaKey(schemaKey)
                .stream()
                .filter(claim -> claim.getOwner().equals(student))
                .collect(Collectors.toList());
    }
}
