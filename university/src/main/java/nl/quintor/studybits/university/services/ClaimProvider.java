package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.university.entities.AuthEncryptedMessage;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public abstract class ClaimProvider<T extends nl.quintor.studybits.university.dto.Claim> {

    protected final UniversityService universityService;
    protected final ClaimRecordRepository claimRecordRepository;
    protected final UserRepository userRepository;
    protected final Mapper mapper;

    protected AuthEncryptedMessage toEntity(AuthcryptedMessage authcryptedMessage) {
        return mapper.map(authcryptedMessage, AuthEncryptedMessage.class);
    }

    protected AuthcryptedMessage toDto(AuthEncryptedMessage authEncryptedMessage) {
        return mapper.map(authEncryptedMessage, AuthcryptedMessage.class);
    }

    protected AuthEncryptedMessageModel toModel(AuthEncryptedMessage authEncryptedMessage) {
        return mapper.map(authEncryptedMessage, AuthEncryptedMessageModel.class);
    }

    protected AuthcryptedMessage toDto(AuthEncryptedMessageModel authEncryptedMessageModel) {
        return mapper.map(authEncryptedMessageModel, AuthcryptedMessage.class);
    }

    public abstract String getSchemaName();

    protected abstract T getClaimForClaimRecord(ClaimRecord claimRecord);

    /**
     * Adds an available claim to the user.
     * Note: It only makes the claim available to the user without being added/written to the ledger.
     * @param userId The id of the user that will receive the claim.
     * @param claim The claim to make available.
     */
    protected void addAvailableClaim(Long userId, T claim) {
        User user = userRepository.getOne(userId);
        ClaimRecord claimRecord = new ClaimRecord(null, user, claim.getSchemaName(), claim.getSchemaVersion(), null, claim.getLabel(), null, null);
        claimRecordRepository.save(claimRecord);
    }

    /**
     * Retrieves the authcrypted claim offer after adding it to the ledger.
     * Note: The cached message will be returned if the claim offer was requested earlier and therefore already written
     *       to the ledger.
     * @param userId The user id.
     * @param claimRecordId The id of the ClaimRecord.
     * @return Authcrypted claim offer.
     */
    @SneakyThrows
    @Transactional
    public AuthEncryptedMessageModel getClaimOffer(Long userId, Long claimRecordId) {
        ClaimRecord claimRecord = getClaimRecord(claimRecordId);
        Validate.validState(claimRecord.getUser().getId().equals(userId), "Claim record user mismatch.");
        User user = getConnectedUserById(userId);
        if (claimRecord.getClaimMessage() != null) {
            return toModel(claimRecord.getClaimOfferMessage());
        }
        Issuer issuer = universityService.getIssuer(user.getUniversity().getName());
        nl.quintor.studybits.university.dto.Claim claim = getClaimForClaimRecord(claimRecord);
        ClaimOffer claimOffer = createClaimOffer(issuer, claimRecord.getUser(), claim.getSchemaDefinition());
        AuthEncryptedMessage authEncryptedMessage = authEncrypt(issuer, claimOffer);
        claimRecord.setClaimNonce(claimOffer.getNonce());
        claimRecord.setClaimOfferMessage(authEncryptedMessage);
        claimRecordRepository.saveAndFlush(claimRecord);
        return toModel(authEncryptedMessage);
    }

    /**
     * Retrieves the authcrypted claim.
     * Note: The cached message will be returned if the claim was requested earlier and therefore already written
     *       to the ledger.
     * @param userId The user id.
     * @param authEncryptedMessageModel The authcrypted ClaimRequest message that was provided to the client by Indy.
     * @return Authcrypted claim.
     */
    @SneakyThrows
    @Transactional
    public AuthEncryptedMessageModel getClaim(Long userId, AuthEncryptedMessageModel authEncryptedMessageModel) {
        User user = getConnectedUserById(userId);
        Issuer issuer = universityService.getIssuer(user.getUniversity().getName());
        ClaimRequest claimRequest = authDecrypt(issuer, authEncryptedMessageModel, ClaimRequest.class);
        ClaimRecord claimRecord = getClaimRecord(claimRequest);
        Validate.validState(claimRecord.getUser().getId().equals(userId), "Claim record user mismatch.");
        if (claimRecord.getClaimMessage() != null) {
            return toModel(claimRecord.getClaimMessage());
        }
        T claim = getClaimForClaimRecord(claimRecord);
        Claim indyClaim = createClaim(issuer, claimRequest, claim);
        AuthEncryptedMessage authEncryptedMessage = authEncrypt(issuer, indyClaim);
        claimRecord.setClaimOfferMessage(authEncryptedMessage);
        claimRecordRepository.saveAndFlush(claimRecord);
        return toModel(authEncryptedMessage);
    }

    @SneakyThrows
    private AuthEncryptedMessage authEncrypt(Issuer issuer, AuthCryptable authCryptable) {
        AuthcryptedMessage authcryptedMessage = issuer.authcrypt(authCryptable).get();
        return toEntity(authcryptedMessage);
    }


    @SneakyThrows
    private <R extends AuthCryptable> R authDecrypt(Issuer issuer, AuthEncryptedMessageModel authEncryptedMessageModel, Class<R> valueType) {
        AuthcryptedMessage authcryptedMessage = toDto(authEncryptedMessageModel);
        return issuer.authDecrypt(authcryptedMessage, valueType).get();
    }

    protected User getConnectedUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found."));
        Validate.validState(user.getConnection() != null, "User onboarding incomplete!");
        return user;
    }

    protected ClaimRecord getClaimRecord(Long claimRecordId) {
        return claimRecordRepository
                .findById(claimRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Claim record not found."));
    }

    protected ClaimRecord getClaimRecord(ClaimRequest claimRequest) {
        ClaimRecord example = new ClaimRecord();
        example.setClaimName(claimRequest.getSchemaKey().getName());
        example.setClaimVersion(claimRequest.getSchemaKey().getVersion());
        example.setClaimNonce(claimRequest.getNonce());
        return claimRecordRepository
                .findOne(Example.of(example))
                .orElseThrow(() -> new IllegalArgumentException("Claim record not found."));
    }


    private void validateClaimRecord(Long userId, ClaimRecord claimRecord) {
        User user = claimRecord.getUser();
        Validate.validState(user.getId().equals(userId), "Claim record user mismatch.");
        Validate.validState(user.getConnection() != null, "User onboarding incomplete!");
    }

    @SneakyThrows
    private ClaimOffer createClaimOffer(Issuer issuer, User user, SchemaDefinition schemaDefinition) {
        String studentDid = user.getConnection().getDid();
        SchemaKey schemaKey = SchemaKey.fromSchema(schemaDefinition, issuer.getIssuerDid());
        return issuer.createClaimOffer(schemaKey, studentDid).get();
    }

    @SneakyThrows
    private Claim createClaim(Issuer issuer, ClaimRequest claimRequest, T claim) {
        return issuer.createClaim(claimRequest, claim.toMap()).get();
    }

}