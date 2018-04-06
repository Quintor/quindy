package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
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
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Version getProofVersion() {
        return ClaimUtils.getVersion(getProofType());
    }

    public String getProofName() {
        return getProofVersion().getName();
    }

    public ProofRecord addProofRequest(Long userId) {
        User user = userRepository.getOne(userId);
        Version version = ClaimUtils.getVersion(getProofType());
        String nonce = RandomStringUtils.randomAlphanumeric(28, 36);
        ProofRecord proofRecord = new ProofRecord(null, user, version.getName(), version.getVersion(), nonce, null);
        return proofRecordRepository.save(proofRecord);
    }


    public AuthcryptedMessage getProofRequest(Long proofRecordId) {
        ProofRecord proofRecord = proofRecordRepository.getOne(proofRecordId);
        Version version = ClaimUtils.getVersion(getProofType());
        Validate.isTrue(version.getName().equals(proofRecord.getProofName()), "Proof name mismatch.");
        Validate.isTrue(version.getVersion().equals(proofRecord.getProofVersion()), "Proof version mismatch.");
        User user = Objects.requireNonNull(proofRecord.getUser());
        University university = Objects.requireNonNull(user.getUniversity());
        String theirDid = Objects.requireNonNull(user.getConnection()).getDid();
        ProofRequest proofRequest = ProofRequest
                .builder()
                .name(version.getName())
                .version(version.getVersion())
                .nonce(proofRecord.getNonce())
                .theirDid(theirDid)
                .requestedAttrs(getRequestedAttributes(university.getId()))
                .build();
        return universityService.authEncrypt(university.getName(), proofRequest);
    }

    public Boolean HandleProof(Long proofRecordId, AuthcryptedMessage authcryptedMessage) {
        // TODO handle proof
        log.warn("TODO: Handle proof {}", proofRecordId);
        return true;
    }

    private Map<String, AttributeInfo> getRequestedAttributes(Long universityId) {
        List<ProofAttribute> proofAttributes = ClaimUtils.getProofAttributes(getProofType());
        Map<Version, Optional<ClaimSchema>> claimSchemaLookup = proofAttributes
                .stream()
                .flatMap(proofAttribute -> proofAttribute.getSchemaVersions().stream())
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
        SchemaKey schemaKey = toSchemaKey(claimSchema);
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

    private SchemaKey toSchemaKey(ClaimSchema claimSchema) {
        return new SchemaKey(claimSchema.getSchemaName(), claimSchema.getSchemaVersion(), claimSchema.getSchemaIssuerDid());
    }

}
