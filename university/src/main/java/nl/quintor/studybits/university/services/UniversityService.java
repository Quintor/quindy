package nl.quintor.studybits.university.services;


import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.university.dto.AuthCryptableResult;
import nl.quintor.studybits.university.dto.Claim;
import nl.quintor.studybits.university.dto.ClaimIssuerSchema;
import nl.quintor.studybits.university.dto.UniversityIssuer;
import nl.quintor.studybits.university.entities.*;
import nl.quintor.studybits.university.repositories.*;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UniversityService {

    private static final boolean LAZY_ISSUER_CREATION = false;

    private final UniversityRepository universityRepository;
    private final ClaimSchemaRepository claimSchemaRepository;
    private final SchemaDefinitionRepository schemaDefinitionRepository;
    private final IssuerService issuerService;
    private final UserRepository userRepository;
    private final ClaimIssuerRepository claimIssuerRepository;
    private final Mapper mapper;


    public List<University> findAll() {
        return universityRepository.findAll();
    }

    public University create(String universityName) {
        University university = universityRepository.save(new University(null, null, universityName, new ArrayList<>(), new ArrayList<>()));
        User user = new User(university);
        userRepository.saveAndFlush(user);

        university.setUser(user);
        universityRepository.save(university);

        if (!LAZY_ISSUER_CREATION) {
            issuerService.ensureIssuer(universityName);
        }
        return university;
    }

    public Optional<University> findUniversity(String universityName) {
        return universityRepository
                .findByNameIgnoreCase(universityName);
    }

    public University getUniversity(String universityName) {
        return findUniversity(universityName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown university."));
    }

    @Transactional
    public UniversityIssuer getUniversityIssuer(String universityName) {
        University university = getUniversity(universityName);
        List<SchemaKey> definedSchemaKeys = university
                .getClaimSchemas()
                .stream()
//                .filter(ClaimSchema::getClaimDefined)
                .map(ServiceUtils::convertToSchemaKey)
                .collect(Collectors.toList());
        Issuer issuer = getIssuer(university.getName());
        return new UniversityIssuer(universityName, issuer.getIssuerDid(), definedSchemaKeys);
    }

    @SneakyThrows
    @Transactional
    public SchemaKey defineSchema(String universityName, SchemaDefinition schemaDefinition) {
        Issuer issuer = getIssuer(universityName);
        SchemaKey schemaKey = issuer.createAndSendSchema(schemaDefinition).get();
        addSchema(universityName, schemaKey);
        return schemaKey;
    }

    @SneakyThrows
    @Transactional
    public void addSchema(String universityName, SchemaKey schemaKey) {
        University university = getUniversity(universityName);
        ClaimSchema claimSchema = new ClaimSchema(university, schemaKey.getName(), schemaKey.getVersion(), schemaKey.getDid());
        university.getClaimSchemas().add(claimSchema);
        universityRepository.save(university);
    }

    @SneakyThrows
    @Transactional
    public void defineClaim(String universityName, SchemaDefinition schemaDefinition) {
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);
        Validate.isTrue(!claimSchema.getClaimDefined(), "Claim already defined.");
        SchemaKey schemaKey = ServiceUtils.convertToSchemaKey(claimSchema);
        Issuer issuer = getIssuer(universityName);
        issuer.defineClaim(schemaKey).get();
        claimSchema.setClaimDefined(true);
        claimSchemaRepository.save(claimSchema);
    }


    public SchemaKey getSchemaKey(String universityName, SchemaDefinition schemaDefinition) {
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);
        return new SchemaKey(claimSchema.getSchemaName(), claimSchema.getSchemaVersion(), claimSchema.getSchemaIssuerDid());
    }

    @SneakyThrows
    public ConnectionRequest createConnectionRequest(String universityName, String userName, String role) {
        Issuer issuer = getIssuer(universityName);
        return issuer.createConnectionRequest(userName, role)
                .get();
    }

    @SneakyThrows
    public String acceptConnectionResponse(String universityName, AnoncryptedMessage anoncryptedConnectionResponseMessage) {
        Issuer issuer = getIssuer(universityName);
        return issuer.anonDecrypt(anoncryptedConnectionResponseMessage, ConnectionResponse.class)
                .thenCompose(AsyncUtil.wrapException(issuer::acceptConnectionResponse))
                .get();
    }


    @SneakyThrows
    public AuthCryptableResult<ClaimOffer> createClaimOffer(String universityName, User user, SchemaDefinition schemaDefinition) {
        IndyConnection indyConnection = Objects.requireNonNull(user.getConnection(), "User onboarding incomplete!");
        Issuer issuer = getIssuer(universityName);
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);
        SchemaKey schemaKey = ServiceUtils.convertToSchemaKey(claimSchema);
        log.info("Creating claim offer with schemaKey: {}, and did: {}", schemaKey, indyConnection.getDid());
        ClaimOffer claimOffer = issuer.createClaimOffer(schemaKey, indyConnection.getDid()).get();
        AuthcryptedMessage authcryptedMessage = issuer.authEncrypt(claimOffer).get();
        return new AuthCryptableResult<>(claimOffer, authcryptedMessage);
    }

    @SneakyThrows
    public <T extends Claim> AuthCryptableResult<nl.quintor.studybits.indy.wrapper.dto.Claim> createClaim(
            String universityName,
            ClaimRequest claimRequest,
            T claim) {
        Issuer issuer = getIssuer(universityName);
        nl.quintor.studybits.indy.wrapper.dto.Claim indyClaim = issuer.createClaim(claimRequest, claim.toMap()).get();
        AuthcryptedMessage authcryptedMessage = issuer.authEncrypt(indyClaim).get();
        return new AuthCryptableResult<>(indyClaim, authcryptedMessage);
    }

    @Transactional
    public void addClaimIssuerForSchema(String universityName, ClaimIssuerSchema claimIssuerSchema) {
        log.debug("University '{}': Adding claim issuer schema information: {}", universityName, claimIssuerSchema);
        SchemaKey schemaKey = claimIssuerSchema.getSchemaKey();
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaKey.getName(), schemaKey.getVersion());
        ClaimIssuer claimIssuer = getClaimIssuer(claimIssuerSchema);
        claimSchema.getClaimIssuers().add(claimIssuer);
        claimSchemaRepository.save(claimSchema);
    }

    private ClaimIssuer getClaimIssuer(ClaimIssuerSchema claimIssuerSchema) {
        return claimIssuerRepository
                .findByDid(claimIssuerSchema.getClaimIssuerDid())
                .orElseGet(() -> new ClaimIssuer(claimIssuerSchema.getClaimIssuerName(), claimIssuerSchema.getClaimIssuerDid()));
    }

    @SneakyThrows
    public List<ProofAttribute> getVerifiedProofAttributes(String universityName, ProofRequest proofRequest, Proof proof) {
        Issuer issuer = getIssuer(universityName);
        return issuer.getVerifiedProofAttributes(proofRequest, proof).get();
    }


    public AuthcryptedMessage authEncrypt(String universityName, AuthCryptable authCryptable) {
        return authEncrypt(getIssuer(universityName), authCryptable);
    }

    public <R extends AuthCryptable> R authDecrypt(String universityName, AuthcryptedMessage authcryptedMessage, Class<R> valueType) {
        return authDecrypt(getIssuer(universityName), authcryptedMessage, valueType);
    }

    private Issuer getIssuer(String universityName) {
        return issuerService.ensureIssuer(universityName);
    }

    @SneakyThrows
    private AuthcryptedMessage authEncrypt(Issuer issuer, AuthCryptable authCryptable) {
        return issuer.authEncrypt(authCryptable).get();
    }

    @SneakyThrows
    private <R extends AuthCryptable> R authDecrypt(Issuer issuer, AuthcryptedMessage authcryptedMessage, Class<R> valueType) {
        return issuer.authDecrypt(authcryptedMessage, valueType).get();
    }


    private ClaimSchema getClaimSchema(String universityName, SchemaDefinition schemaDefinition) {
        return getClaimSchema(universityName, schemaDefinition.getName(), schemaDefinition.getVersion());
    }

    private ClaimSchema getClaimSchema(String universityName, String schemaName, String schemaVersion) {
        return claimSchemaRepository
                .findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(universityName, schemaName, schemaVersion)
                .orElseThrow(() -> new IllegalArgumentException("Schema key not found."));
    }

    public List<SchemaDefinitionRecord> getSchemaDefinitions(String universityName) {
        if (schemaDefinitionRepository.count() == 0)
            fetchSchemaDefinitionsFromLedger(universityName);

        return schemaDefinitionRepository.findAll();
    }

    private void fetchSchemaDefinitionsFromLedger(String universityName) {
        Issuer issuer = getIssuer(universityName);
        getUniversity(universityName)
                .getClaimSchemas()
                .forEach(claimSchema -> getSchemaDefinition(issuer, claimSchema));
    }

    private SchemaDefinitionRecord getSchemaDefinition(Issuer issuer, ClaimSchema claimSchema) {
        return schemaDefinitionRepository
                .findByNameIgnoreCaseAndVersion(claimSchema.getSchemaName(), claimSchema.getSchemaVersion())
                .orElseGet(() -> getSchemaDefinitionFromSchemaKey(issuer, claimSchema));
    }

    private SchemaDefinitionRecord getSchemaDefinitionFromSchemaKey(Issuer issuer, ClaimSchema claimSchema) {
        try {
            SchemaKey schemaKey = new SchemaKey(claimSchema.getSchemaName(), claimSchema.getSchemaVersion(), claimSchema.getSchemaIssuerDid());
            SchemaDefinitionRecord record = mapper.map(issuer.getSchema(schemaKey.getDid(), schemaKey).get().getData(), SchemaDefinitionRecord.class);
            return schemaDefinitionRepository.saveAndFlush(record);
        } catch (Exception e) {
            log.error("{}, Could not get SchemaDefinition for SchemaKey {}", e, claimSchema);
            throw new IllegalArgumentException(String.format("Could not find SchemaDefinition for name: %s and version: %s", claimSchema.getSchemaName(), claimSchema.getSchemaVersion()));
        }
    }
}