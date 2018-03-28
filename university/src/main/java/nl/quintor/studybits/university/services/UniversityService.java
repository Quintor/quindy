package nl.quintor.studybits.university.services;


import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.university.dto.AuthCryptableResult;
import nl.quintor.studybits.university.dto.Claim;
import nl.quintor.studybits.university.entities.*;
import nl.quintor.studybits.university.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.university.models.UniversityModel;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import nl.quintor.studybits.university.repositories.UniversityRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class UniversityService {

    private static final boolean LAZY_ISSUER_CREATION = true;

    private final UniversityRepository universityRepository;
    private final ClaimSchemaRepository claimSchemaRepository;
    private final IssuerService issuerService;
    private final Mapper mapper;

    private UniversityModel toModel(University university ) {
        return mapper.map(university, UniversityModel.class);
    }

    private University toEntity(UniversityModel universityModel ) {
        return mapper.map(universityModel, University.class);
    }

//    private AuthEncryptedMessage toEntity(AuthcryptedMessage authcryptedMessage) {
//        return mapper.map(authcryptedMessage, AuthEncryptedMessage.class);
//    }
//
//    private AuthcryptedMessage toDto(AuthEncryptedMessage authEncryptedMessage) {
//        return mapper.map(authEncryptedMessage, AuthcryptedMessage.class);
//    }
//
//    private AuthEncryptedMessageModel toModel(AuthEncryptedMessage authEncryptedMessage) {
//        return mapper.map(authEncryptedMessage, AuthEncryptedMessageModel.class);
//    }
//
//    private AuthcryptedMessage toDto(AuthEncryptedMessageModel authEncryptedMessageModel) {
//        return mapper.map(authEncryptedMessageModel, AuthcryptedMessage.class);
//    }

    public List<UniversityModel> findAll() {
        return universityRepository.findAll()
                                   .stream()
                                   .map(this::toModel)
                                   .collect(Collectors.toList());
    }


    public UniversityModel create(String universityName) {
        University university = universityRepository.save(new University(null, universityName, new ArrayList<>()));
        if(!LAZY_ISSUER_CREATION) {
            issuerService.ensureIssuer(universityName);
        }
        return toModel(university);
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
        ClaimSchema claimSchema = new ClaimSchema(null, university, schemaKey.getName(), schemaKey.getVersion(), schemaKey.getDid(), false);
        university.getClaimSchemas().add(claimSchema);
        universityRepository.save(university);
    }


    @SneakyThrows
    @Transactional
    public void defineClaim(String universityName, SchemaDefinition schemaDefinition) {
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);
        Validate.isTrue(!claimSchema.getClaimDefined(), "Claim already defined.");
        SchemaKey schemaKey = toSchemaKey(claimSchema);
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
    public AuthCryptableResult<ClaimOffer> createClaimOffer(String universityName, User user, SchemaDefinition schemaDefinition) {
        IndyConnection indyConnection = user.getConnection();
        Validate.validState(indyConnection != null, "User onboarding incomplete!");
        Issuer issuer = getIssuer(universityName);
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);
        SchemaKey schemaKey = toSchemaKey(claimSchema);
        ClaimOffer claimOffer = issuer.createClaimOffer(schemaKey, indyConnection.getDid()).get();
        AuthcryptedMessage authcryptedMessage = issuer.authcrypt(claimOffer).get();
        return new AuthCryptableResult<>(claimOffer, authcryptedMessage);
    }

    @SneakyThrows
    public <T extends Claim> AuthCryptableResult<nl.quintor.studybits.indy.wrapper.dto.Claim> createClaim(
            String universityName,
            ClaimRequest claimRequest,
            T claim) {
        Issuer issuer = getIssuer(universityName);
        nl.quintor.studybits.indy.wrapper.dto.Claim indyClaim = issuer.createClaim(claimRequest, claim.toMap()).get();
        AuthcryptedMessage authcryptedMessage = issuer.authcrypt(indyClaim).get();
        return new AuthCryptableResult<>(indyClaim, authcryptedMessage);
    }

    public AuthcryptedMessage authEncrypt(String universityName, AuthCryptable authCryptable) {
        return authEncrypt(getIssuer(universityName), authCryptable);
    }

    public  <R extends AuthCryptable> R authDecrypt(String universityName, AuthcryptedMessage authcryptedMessage, Class<R> valueType) {
        return authDecrypt(getIssuer(universityName), authcryptedMessage, valueType);
    }

    public Issuer getIssuer(String universityName) {
        return issuerService.ensureIssuer(universityName);
    }

    @SneakyThrows
    private AuthcryptedMessage authEncrypt(Issuer issuer, AuthCryptable authCryptable) {
        AuthcryptedMessage authcryptedMessage = issuer.authcrypt(authCryptable).get();
        return authcryptedMessage;
    }


    @SneakyThrows
    private <R extends AuthCryptable> R authDecrypt(Issuer issuer, AuthcryptedMessage authcryptedMessage, Class<R> valueType) {
        return issuer.authDecrypt(authcryptedMessage, valueType).get();
    }

    private ClaimSchema getClaimSchema(String universityName, SchemaDefinition schemaDefinition) {
        return claimSchemaRepository
                .findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(
                        universityName,
                        schemaDefinition.getName(),
                        schemaDefinition.getVersion()
                ).orElseThrow(() -> new IllegalArgumentException("Schema key not found."));
    }

    private SchemaKey toSchemaKey(ClaimSchema claimSchema) {
        return new SchemaKey(claimSchema.getSchemaName(), claimSchema.getSchemaVersion(), claimSchema.getSchemaIssuerDid());
    }

    private University getUniversity(String universityName) {
        return universityRepository
                .findByNameIgnoreCase(universityName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown university."));
    }

}