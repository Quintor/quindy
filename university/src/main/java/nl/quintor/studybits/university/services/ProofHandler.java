package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.university.dto.ClaimUtils;
import nl.quintor.studybits.university.dto.Proof;
import nl.quintor.studybits.university.dto.ProofAttribute;
import nl.quintor.studybits.university.dto.Version;
import nl.quintor.studybits.university.entities.*;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public abstract class ProofHandler<T extends Proof> {

    protected final UniversityService universityService;
    protected final ProofRecordRepository proofRecordRepository;
    protected final ClaimSchemaRepository claimSchemaRepository;
    protected final UserRepository userRepository;
    protected final Mapper mapper;

    private AuthcryptedMessage toDto(AuthEncryptedMessage authEncryptedMessage) {
        return mapper.map(authEncryptedMessage, AuthcryptedMessage.class);
    }

    protected abstract Class<T> getProofType();

    @Transactional
    protected abstract boolean handleProof(User user, ProofRecord proofRecord, T proof);

    @SneakyThrows
    private T newProof() {
        return getProofType().newInstance();
    }

    public Version getProofVersion() {
        return ClaimUtils.getVersion(getProofType());
    }

    public String getProofName() {
        return getProofVersion().getName();
    }

    public ProofRecord addProofRequest(Long userId) {
        User user = userRepository.getOne(userId);
        Version version = ClaimUtils.getVersion(getProofType());
        String nonce = RandomStringUtils.randomNumeric(28, 36);
        ProofRecord proofRecord = new ProofRecord(null, user, version.getName(), version.getVersion(), nonce, null);
        return proofRecordRepository.save(proofRecord);
    }


    public AuthcryptedMessage getProofRequestMessage(Long userId, Long proofRecordId) {
        ProofRecord proofRecord = getProofRecord(userId, proofRecordId);
        User user = Objects.requireNonNull(proofRecord.getUser());
        University university = Objects.requireNonNull(user.getUniversity());
        ProofRequest proofRequest = getProofRequest(university, user, proofRecord);
        return universityService.authEncrypt(university.getName(), proofRequest);
    }

    @Transactional
    public Boolean handleProof(Long userId, Long proofRecordId, AuthcryptedMessage authcryptedMessage) {
        ProofRecord proofRecord = getProofRecord(userId, proofRecordId);
        Validate.validState(StringUtils.isEmpty(proofRecord.getProofJson()), String.format("UserId %s already provided proof for proofRecordId %s.", userId, proofRecord));
        User user = Objects.requireNonNull(proofRecord.getUser());
        University university = Objects.requireNonNull(user.getUniversity());
        ProofRequest proofRequest = getProofRequest(university, user, proofRecord);
        T verifiedProof = getVerifiedProof(university.getName(), proofRequest, authcryptedMessage);
        proofRecord.setProofJson(ServiceUtils.objectToJson(verifiedProof));
        Boolean handled = handleProof(user, proofRecord, verifiedProof);
        if(handled) {
            proofRecordRepository.save(proofRecord);
        }
        return handled;
    }

    public T getProof(Long userId, Long proofRecordId) {
        ProofRecord proofRecord = getProofRecord(userId, proofRecordId);
        Validate.validState(StringUtils.isEmpty(proofRecord.getProofJson()), String.format("UserId %s did not provide proof for proofRecordId %s.", userId, proofRecord));
        User user = Objects.requireNonNull(proofRecord.getUser());
        return ServiceUtils.jsonToObject(proofRecord.getProofJson(), getProofType());
    }

    private T getVerifiedProof(String universityName, ProofRequest proofRequest, AuthcryptedMessage authcryptedMessage) {
        nl.quintor.studybits.indy.wrapper.dto.Proof proof = universityService
                .authDecrypt(universityName, authcryptedMessage, nl.quintor.studybits.indy.wrapper.dto.Proof.class);
        List<nl.quintor.studybits.indy.wrapper.dto.ProofAttribute> attributes = universityService
                .getVerifiedProofAttributes(universityName, proofRequest, proof);
        T result = newProof();
        attributes.forEach(attribute -> writeAttributeField(result, attribute));
        return result;
    }

    private void writeAttributeField(T result, nl.quintor.studybits.indy.wrapper.dto.ProofAttribute proofAttribute) {
        try {
            FieldUtils.writeField(result, proofAttribute.getKey(), proofAttribute.getValue(), true);
        } catch (IllegalAccessException e) {
            String message = String.format("Unable to write proof attribute to field '%s' of type '%s'.",proofAttribute.getKey(), result.getClass().getName());
            throw new IllegalStateException(message, e);
        }
    }

    private ProofRequest getProofRequest(University university, User user, ProofRecord proofRecord) {
        String theirDid = Objects.requireNonNull(user.getConnection()).getDid();
        return ProofRequest
                .builder()
                .name(proofRecord.getProofName())
                .version(proofRecord.getProofVersion())
                .nonce(proofRecord.getNonce())
                .theirDid(theirDid)
                .requestedAttrs(getRequestedAttributes(university.getId()))
                .build();
    }

    private Map<String, AttributeInfo> getRequestedAttributes(Long universityId) {
        List<ProofAttribute> proofAttributes = ClaimUtils.getProofAttributes(getProofType());
        Map<Version, Optional<ClaimSchema>> claimSchemaLookup = proofAttributes
                .stream()
                .flatMap(proofAttribute -> proofAttribute.getSchemaVersions().stream())
                .distinct()
                .collect(Collectors.toMap(v -> v, v -> findClaimSchema(universityId, v)));

        return proofAttributes
                .stream()
                .collect(Collectors.toMap(
                        ProofAttribute::getFieldName,
                        proofAttribute -> getAttributeInfo(proofAttribute, claimSchemaLookup))
                );
    }

    private AttributeInfo getAttributeInfo(ProofAttribute proofAttribute, Map<Version, Optional<ClaimSchema>> claimSchemaLookup) {
        List<Version> versions = proofAttribute.getSchemaVersions();
        if(versions.isEmpty()) {
            return new AttributeInfo(proofAttribute.getAttributeName(), Optional.empty());
        }

        List<Filter> filters = versions
                .stream()
                .map(claimSchemaLookup::get)
                .flatMap(this::createFilter)
                .collect(Collectors.toList());
        Validate.notEmpty(filters, "No claim issuers found for field '%s'.", proofAttribute.getField());
        return new AttributeInfo(proofAttribute.getAttributeName(), Optional.of(filters));
    }

    private Stream<Filter> createFilter(Optional<ClaimSchema> claimSchema) {
        return claimSchema
                .map(this::createFilter)
                .orElseGet(Stream::empty);
    }

    private Stream<Filter> createFilter(ClaimSchema claimSchema) {
        SchemaKey schemaKey = ServiceUtils.convertToSchemaKey(claimSchema);
        return claimSchema
                .getClaimIssuers()
                .stream()
                .map(claimIssuer -> new Filter(claimIssuer.getDid(), schemaKey));
    }


    private Optional<ClaimSchema> findClaimSchema(Long universityId, Version schemaVersion) {
        return claimSchemaRepository.findByUniversityIdAndSchemaNameAndSchemaVersion(
                universityId,
                schemaVersion.getName(),
                schemaVersion.getVersion());
    }


    private ProofRecord getProofRecord(Long userId, Long proofRecordId) {
        ProofRecord proofRecord = proofRecordRepository
                .findById(proofRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Proof record not found."));
        User user = Objects.requireNonNull(proofRecord.getUser(), "Proof record without user.");
        Validate.validState(user.getId().equals(userId), "Proof record user mismatch.");
        Validate.notNull(user.getConnection(), "User onboarding incomplete!");
        Version version = getProofVersion();
        Validate.isTrue(version.getName().equals(proofRecord.getProofName()), "Proof name mismatch.");
        Validate.isTrue(version.getVersion().equals(proofRecord.getProofVersion()), "Proof version mismatch.");
        return proofRecord;
    }

}
