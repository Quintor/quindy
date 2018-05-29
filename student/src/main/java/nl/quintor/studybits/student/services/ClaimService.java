package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.student.entities.*;
import nl.quintor.studybits.student.entities.SchemaKey;
import nl.quintor.studybits.student.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.student.models.ClaimOfferModel;
import nl.quintor.studybits.student.models.StudentClaimInfoModel;
import nl.quintor.studybits.student.repositories.ClaimRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
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
    private IndyPool indyPool;
    private MetaWalletService metaWalletService;
    private StudentService studentService;
    private StudentProverService studentProverService;
    private Mapper mapper;

    /**
     * Retrieves all ClaimInfo, ClaimOffers, and Claims from all Universities, which are connected to a student.
     * Claims which are not saved yet, are saved. The rest is disregarded.
     *
     * @param studentUserName: The userName of the student for which Claims should be fetched.
     * @throws Exception
     */
    @Transactional
    public void getAndSaveNewClaimsForOwnerUserName(String studentUserName) throws Exception {
        Student student = studentService.getByUserName(studentUserName);
        log.info("Get and saving new claims for student: {}", student);
        studentProverService.withProverForStudent(student, prover -> {
            getAllStudentClaimInfo(student)
                    .filter(this::isNewClaimInfo)
                    .forEach((StudentClaimInfoModel claimInfo) -> {
                        try {
                            ClaimOfferModel claimOffer = getClaimOfferForClaimInfo(claimInfo, prover);
                            CredentialWithRequest claim = getClaimFromUniversity(claimOffer, prover);

                            prover.storeCredential(claim);
                            saveClaimIfNew(claim.getCredential(), student, claimInfo);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    });
        });
    }

    private boolean isNewClaimInfo(StudentClaimInfoModel claimInfoModel) {
        boolean exists = claimRepository.existsBySchemaIdAndLabel(
                claimInfoModel.getSchemaId(),
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

        return getClaimOfferModelFromAuthEncryptedMessageModel(authEncryptedMessageModel, prover);
    }

    /**
     * Saves a Claim to the database, if the claim does not yet exist in the database.
     * The hash of the claim values is used to determine whether a claim is stored yet.
     */
    private void saveClaimIfNew(Credential claim, Student student, StudentClaimInfoModel claimInfo) {
        ClaimEntity claimEntity = mapper.map(claim, ClaimEntity.class);

        claimEntity.setStudent(student);
        claimEntity.setLabel(claimInfo.getLabel());

        log.info("Created claimEntity {} from credential {}", claimEntity, claim);
        if(!claimRepository.existsBySchemaIdAndLabel(claimEntity.getSchemaId(), claimEntity.getLabel()))
            claimRepository.save(claimEntity);
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

        return new RestTemplate().exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<List<StudentClaimInfoModel>>() {})
                .getBody()
                .stream();
    }

    private ClaimOfferModel getClaimOfferModelFromAuthEncryptedMessageModel(AuthEncryptedMessageModel authEncryptedMessageModel, Prover prover) throws Exception {
        ClaimOfferModel claimOfferModel = new ClaimOfferModel();
        authEncryptedMessageModel.getLinks().forEach(claimOfferModel::add);

        AuthcryptedMessage authcryptedMessage = mapper.map(authEncryptedMessageModel, AuthcryptedMessage.class);
        CredentialOffer claimOffer = decryptAuthcryptedMessage(authcryptedMessage, prover, CredentialOffer.class).get();
        claimOfferModel.setClaimOffer(claimOffer);
        return claimOfferModel;
    }

    private CredentialWithRequest getClaimFromUniversity(ClaimOfferModel claimOfferModel, Prover prover) throws Exception {
        AuthEncryptedMessageModel encryptedClaimRequest = getEncryptedClaimRequestForClaimOffer(claimOfferModel.getClaimOffer(), prover);

        log.debug("Retrieving ClaimModel from UniversityModel with claimOffer {} ", claimOfferModel);
        AuthEncryptedMessageModel response = new RestTemplate().postForObject(claimOfferModel.getLink("self")
                .getHref(), encryptedClaimRequest, AuthEncryptedMessageModel.class);

        AuthcryptedMessage authcryptedMessage = mapper.map(response, AuthcryptedMessage.class);
        return decryptAuthcryptedMessage(authcryptedMessage, prover, CredentialWithRequest.class).get();
    }

    private AuthEncryptedMessageModel getEncryptedClaimRequestForClaimOffer(CredentialOffer claimOffer, Prover prover) throws Exception {
        log.debug("Creating ClaimRequest with claimOffer {}", claimOffer);
        return prover.createCredentialRequest(claimOffer)
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

    public ClaimEntity getById(Long claimId) {
        return claimRepository
                .findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("ClaimModel with id not found"));
    }

    public List<ClaimEntity> findAllByOwnerUserName(String studentUserName) {
        Student student = studentService.getByUserName(studentUserName);
        return claimRepository.findAllByStudentId(student.getId());
    }

    public List<ClaimEntity> findByOwnerUserNameAndSchemaKeyName(String studentUserName, String schemaName) {
        Student student = studentService.getByUserName(studentUserName);
        return claimRepository.findAllByStudentId(student.getId())
                .stream()
                .filter(AsyncUtil.wrapPredicateException(claimEntity -> {
                    try (IndyWallet wallet = metaWalletService.openIndyWalletFromMetaWallet(student.getMetaWallet())) {
                        try (Prover prover = new Prover(student.getUserName(), indyPool, wallet, student.getUserName())) {
                            Schema schema = prover.getSchema(prover.getWallet().getMainDid(), claimEntity.getSchemaId()).get();
                            return schema.getName().equals(schemaName);
                        }
                    }
                }))
                .collect(Collectors.toList());
    }
}
