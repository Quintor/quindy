package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.entities.*;
import nl.quintor.studybits.student.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.student.models.ClaimOfferModel;
import nl.quintor.studybits.student.models.StudentClaimInfoModel;
import nl.quintor.studybits.student.repositories.ClaimRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimService {
    private ClaimRepository claimRepository;
    private ConnectionRecordService connectionRecordService;
    private SchemaKeyService schemaKeyService;
    private StudentService studentService;
    private Mapper mapper;


    private Claim toClaimEntity(nl.quintor.studybits.indy.wrapper.dto.Claim claim, String label) {
        Claim result = mapper.map(claim, Claim.class);
        result.setLabel(label);
        return result;
    }

    /**
     * Retrieves all ClaimInfo, ClaimOffers, and Claims from all Universities, which are connected to a student.
     * Claims which are not saved yet, are saved. The rest is disregarded.
     *
     * @param studentUserName: The userName of the student for which Claims should be fetched.
     * @throws Exception
     */
    public void getAndSaveNewClaimsForOwnerUserName(String studentUserName) throws Exception {
        Student student = studentService.findByNameOrElseThrow(studentUserName);
        try (Prover prover = studentService.getProverForStudent(student)) {
            getAllStudentClaimInfo(student)
                    .filter(this::isNewClaimInfo)
                    .map(AsyncUtil.wrapException(claimInfo -> getClaimOfferForClaimInfo(claimInfo, prover)))
                    .map(AsyncUtil.wrapException(claimOffer -> getClaimForClaimOffer(claimOffer, prover, student)))
                    .forEach(this::saveClaimIfNew);
        }
    }

    private boolean isNewClaimInfo(StudentClaimInfoModel claimInfoModel) {
        boolean exists = claimRepository.existsBySchemaKeyNameAndSchemaKeyVersionAndLabel(
                claimInfoModel.getName(),
                claimInfoModel.getVersion(),
                claimInfoModel.getLabel());
        return !exists;
    }

    /**
     * Retrieves all ClaimInfo from all Universities with which a given Student has connections.
     *
     * @param student: The student for whom the claims should be retrieved.
     * @return All StudentClaimInfoModels as a stream.
     */
    private Stream<StudentClaimInfoModel> getAllStudentClaimInfo(Student student) {
        return connectionRecordService.findAllByStudentUserName(student.getUserName())
                .stream()
                .map(ConnectionRecord::getUniversity)
                .flatMap(university -> getAllStudentClaimInfoFromUniversity(university, student));
    }

    /**
     * Retrieves a ClaimOffer for a given ClaimInfo from the University which holds the ClaimOffer.
     *
     * @param claimInfo: The claimInfo for which the clainOffer should be retrieved.
     * @return A ClaimOffer encrypted with the Did of the connection between Student and University.
     */
    private ClaimOfferModel getClaimOfferForClaimInfo(StudentClaimInfoModel claimInfo, Prover prover) throws Exception {
        AuthEncryptedMessageModel authEncryptedMessageModel = new RestTemplate()
                .exchange(
                        claimInfo.getLink("self").getHref(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<AuthEncryptedMessageModel>() {
                        }
                )
                .getBody();

        ClaimOfferModel claimOfferModel = getClaimOfferModelFromAuthEncryptedMessageModel(authEncryptedMessageModel, prover);
        claimOfferModel.setLabel(claimInfo.getLabel());
        return claimOfferModel;
    }

    /**
     * Retrieves a Claim from the link connected to a ClaimOffer from the University, which holds the Claim.
     *
     * @param claimOfferModel: The claimOffer for a claim which holds a HATEOAS link to the claim object.
     * @param owner:           The student object which owns the claim. Needed to create a prover to decrypt the response.
     * @return a Claim object.
     * @throws Exception
     */
    private Claim getClaimForClaimOffer(ClaimOfferModel claimOfferModel, Prover prover, Student owner) throws Exception {
        Claim claim = getClaimFromUniversity(claimOfferModel, prover, claimOfferModel.getLabel());
        claim.setOwner(owner);
        claim.setHashId(hashClaim(claim));

        return claim;
    }

    /**
     * Saves a Claim to the database, if the claim does not yet exist in the database.
     * The hash of the claim values is used to determine whether a claim is stored yet.
     *
     * @param claim: The claim to be stored.
     */
    private void saveClaimIfNew(Claim claim) {
        if (!claimRepository.existsByHashId(claim.getHashId()))
            claimRepository.save(claim);
    }

    /**
     * Retrieves all claimInfo for a given Student from a certain University.
     *
     * @param university: The university from where to retrieve all claimInfo.
     * @param student:    The student for whom all claimInfo should be retrieved.
     * @return All claimInfo as a stream.
     */
    private Stream<StudentClaimInfoModel> getAllStudentClaimInfoFromUniversity(University university, Student student) {
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

    private ClaimOfferModel getClaimOfferModelFromAuthEncryptedMessageModel(AuthEncryptedMessageModel authEncryptedMessageModel, Prover prover) throws Exception {
        ClaimOfferModel claimOfferModel = new ClaimOfferModel();
        authEncryptedMessageModel.getLinks().forEach(claimOfferModel::add);

        AuthcryptedMessage authcryptedMessage = mapper.map(authEncryptedMessageModel, AuthcryptedMessage.class);
        ClaimOffer claimOffer = decryptAuthcryptedMessage(authcryptedMessage, prover, ClaimOffer.class).get();
        claimOfferModel.setClaimOffer(claimOffer);
        return claimOfferModel;
    }

    private Claim getClaimFromUniversity(ClaimOfferModel claimOfferModel, Prover prover, String label) throws Exception {
        AuthEncryptedMessageModel encryptedClaimRequest = getEncryptedClaimRequestForClaimOffer(claimOfferModel.getClaimOffer(), prover);

        log.debug("Retrieving ClaimModel from UniversityModel with claimOffer {} ", claimOfferModel);
        AuthEncryptedMessageModel response = new RestTemplate().postForObject(claimOfferModel.getLink("self")
                .getHref(), encryptedClaimRequest, AuthEncryptedMessageModel.class);

        AuthcryptedMessage authcryptedMessage = mapper.map(response, AuthcryptedMessage.class);
        return decryptAuthcryptedMessage(authcryptedMessage, prover, nl.quintor.studybits.indy.wrapper.dto.Claim.class)
                .thenApply(wrapperClaim -> toClaimEntity(wrapperClaim, label))
                .get();
    }

    private AuthEncryptedMessageModel getEncryptedClaimRequestForClaimOffer(ClaimOffer claimOffer, Prover prover) throws Exception {
        log.debug("Creating ClaimRequest with claimOffer {}", claimOffer);
        return prover.createClaimRequest(claimOffer)
                .thenCompose(
                        AsyncUtil.wrapException(claimRequest -> {
                            log.debug("AuthEncrypting ClaimRequest {} with Prover.", claimRequest);
                            return prover.authEncrypt(claimRequest);
                        })
                )
                .thenApply(encryptedClaimRequest -> mapper.map(encryptedClaimRequest, AuthEncryptedMessageModel.class))
                .get();
    }

    @SneakyThrows
    private <T extends AuthCryptable> CompletableFuture<T> decryptAuthcryptedMessage(AuthcryptedMessage authMessage, Prover prover, Class<T> type) {
        log.debug("Decrypting AuthcryptedMessage {} with prover {} to class {}", authMessage, prover, type);
        return prover.authDecrypt(authMessage, type);
    }

    private String hashClaim(Claim claim) {
        return DigestUtils.sha256Hex(claim.getValues());
    }

    public Claim findByIdOrElseThrow(Long claimId) {
        return claimRepository
                .findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("ClaimModel with id not found"));
    }

    public List<Claim> findAllByOwnerUserName(String studentUserName) {
        Student owner = studentService.findByNameOrElseThrow(studentUserName);
        return claimRepository.findAllByOwnerId(owner.getId());
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

    public void deleteAll() {
        claimRepository.deleteAll();
    }
}
