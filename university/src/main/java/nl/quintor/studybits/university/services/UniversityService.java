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
import java.sql.SQLOutput;
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
        List<String> definedSchemaKeys = university
                .getClaimSchemas()
                .stream()
                .filter(schema -> schema.getCredentialDefId() != null)
                .map(ClaimSchema::getSchemaId)
                .collect(Collectors.toList());
        Issuer issuer = getIssuer(university.getName());
        return new UniversityIssuer(universityName, issuer.getIssuerDid(), definedSchemaKeys);
    }

    @SneakyThrows
    @Transactional
    public String defineSchema(String universityName, SchemaDefinition schemaDefinition) {
        Issuer issuer = getIssuer(universityName);
        String schemaId = issuer.createAndSendSchema(schemaDefinition.getName(), schemaDefinition.getVersion(), schemaDefinition.getAttrNames().toArray(new String[]{})).get();
        addSchema(universityName, schemaId);
        return schemaId;
    }

    @SneakyThrows
    @Transactional
    public void addSchema(String universityName, String schemaId) {
        University university = getUniversity(universityName);
        Issuer issuer = getIssuer(universityName);
        Schema walletClaimSchema = issuer.getSchema(issuer.getIssuerDid(), schemaId).get();
        ClaimSchema claimSchema = new ClaimSchema(schemaId, university, walletClaimSchema.getName(), walletClaimSchema.getVersion(), issuer.getIssuerDid());
        log.info("Persisting claimSchema {}", claimSchema);
        university.getClaimSchemas().add(claimSchema);
        universityRepository.save(university);
    }

    @SneakyThrows
    @Transactional
    public void defineClaim(String universityName, SchemaDefinition schemaDefinition) {
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);
        Validate.isTrue(claimSchema.getCredentialDefId() == null, "Claim already defined.");

        Issuer issuer = getIssuer(universityName);
        String credentialDefId = issuer.defineCredential(claimSchema.getSchemaId()).get();
        claimSchema.setCredentialDefId(credentialDefId);
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
    public AuthCryptableResult<CredentialOffer> createClaimOffer(String universityName, User user, SchemaDefinition schemaDefinition) {
        IndyConnection indyConnection = Objects.requireNonNull(user.getConnection(), "User onboarding incomplete!");
        Issuer issuer = getIssuer(universityName);
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);

        log.info("Creating claim offer with credDefId: {}, and did: {}", claimSchema.getCredentialDefId(), indyConnection.getDid());
        CredentialOffer claimOffer = issuer.createCredentialOffer(claimSchema.getCredentialDefId(), indyConnection.getDid()).get();
        AuthcryptedMessage authcryptedMessage = issuer.authEncrypt(claimOffer).get();
        return new AuthCryptableResult<>(claimOffer, authcryptedMessage);
    }

    @SneakyThrows
    public <T extends Claim> AuthCryptableResult<nl.quintor.studybits.indy.wrapper.dto.CredentialWithRequest> createClaim(
            String universityName,
            CredentialRequest claimRequest,
            T claim) {
        Issuer issuer = getIssuer(universityName);
        nl.quintor.studybits.indy.wrapper.dto.CredentialWithRequest indyClaim = issuer.createCredential(claimRequest, claim.toMap()).get();
        AuthcryptedMessage authcryptedMessage = issuer.authEncrypt(indyClaim).get();
        return new AuthCryptableResult<>(indyClaim, authcryptedMessage);
    }

    @Transactional
    public void addClaimIssuerForSchema(String universityName, ClaimIssuerSchema claimIssuerSchema) {
        log.debug("University '{}': Adding claim issuer schema information: {}", universityName, claimIssuerSchema);

        String schemaId = claimIssuerSchema.getSchemaId();
        University university = getUniversity(universityName);
        ClaimSchema claimSchema = getClaimSchema(university.getId(), schemaId);
        ClaimIssuer claimIssuer = claimIssuerRepository
                .findByDid(claimIssuerSchema.getClaimIssuerDid())
                .orElseGet(() -> new ClaimIssuer(claimIssuerSchema.getClaimIssuerName(), claimIssuerSchema.getClaimIssuerDid()));

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
        log.info("Finding claimSchema by name and version: {}, {}", schemaName, schemaVersion);
        claimSchemaRepository.findAll().forEach(claimSchema -> System.out.println(claimSchema.toString()));

        return claimSchemaRepository
                .findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(universityName, schemaName, schemaVersion)
                .orElseThrow(() -> new IllegalArgumentException("Schema key not found."));
    }

    private ClaimSchema getClaimSchema(Long universityId, String schemaId) {
        log.info("Finding claimSchema by uniId and schemaId: {}, {}", universityId, schemaId);
        claimSchemaRepository.findAll().forEach(claimSchema -> System.out.println(claimSchema.toString()));
        return claimSchemaRepository.findByUniversityIdAndSchemaId(universityId, schemaId)
                .orElseThrow(() -> new IllegalArgumentException("Schema key not found."));
    }

    public List<SchemaDefinition> getSchemaDefinitions(String universityName) {
        Issuer issuer = getIssuer(universityName);
        return getUniversityIssuer(universityName)
                .getDefinedSchemaIds()
                .stream()
                .map(schemaKey -> getSchemaDefinitionFromSchemaId(issuer, schemaKey))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static SchemaDefinition getSchemaDefinitionFromSchemaId(Issuer issuer, String schemaId) {
        try {
            Schema schema = issuer.getSchema(issuer.getIssuerDid(), schemaId).get();
            return new SchemaDefinition(schema.getName(), schema.getVersion(), schema.getAttrNames());
        } catch (Exception e) {
            log.error("{}, Could not get SchemaDefinition for SchemaKey {}", e, schemaId);
            return null;

        }
    }
}