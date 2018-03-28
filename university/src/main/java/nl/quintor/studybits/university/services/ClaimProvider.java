package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.university.dto.AuthCryptableResult;
import nl.quintor.studybits.university.dto.Claim;
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
public abstract class ClaimProvider<T extends Claim> {

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

    protected AuthEncryptedMessageModel toModel(AuthcryptedMessage authcryptedMessage) {
        return mapper.map(authcryptedMessage, AuthEncryptedMessageModel.class);
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
        ClaimRecord claimRecord = getClaimRecord(userId, claimRecordId);
        if (claimRecord.getClaimMessage() != null) {
            return toModel(claimRecord.getClaimOfferMessage());
        }
        String universityName = claimRecord.getUser().getUniversity().getName();
        Claim claim = getClaimForClaimRecord(claimRecord);
        AuthCryptableResult<ClaimOffer> result = universityService
                .createClaimOffer(universityName, claimRecord.getUser(), claim.getSchemaDefinition());
        claimRecord.setClaimNonce(result.getAuthCryptable().getNonce());
        claimRecord.setClaimOfferMessage(result.getAuthEncryptedMessage());
        claimRecordRepository.saveAndFlush(claimRecord);
        return result.getAuthEncryptedMessageModel();
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
        String universityName = user.getUniversity().getName();
        ClaimRequest claimRequest = universityService.authDecrypt(universityName, toDto(authEncryptedMessageModel), ClaimRequest.class);
        ClaimRecord claimRecord = getClaimRecord(userId, claimRequest);
        if (claimRecord.getClaimMessage() != null) {
            return toModel(claimRecord.getClaimMessage());
        }
        T claim = getClaimForClaimRecord(claimRecord);
        AuthCryptableResult<nl.quintor.studybits.indy.wrapper.dto.Claim> result = universityService
                .createClaim(universityName, claimRequest, claim);
        claimRecord.setClaimOfferMessage(result.getAuthEncryptedMessage());
        claimRecordRepository.saveAndFlush(claimRecord);
        return result.getAuthEncryptedMessageModel();
    }

    protected User getConnectedUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found."));
        Validate.validState(user.getConnection() != null, "User onboarding incomplete!");
        return user;
    }

    protected ClaimRecord getClaimRecord(Long userId, Long claimRecordId) {
        ClaimRecord claimRecord = claimRecordRepository
                .findById(claimRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Claim record not found."));
        Validate.validState(claimRecord.getUser().getId().equals(userId), "Claim record user mismatch.");
        return claimRecord;
    }

    protected ClaimRecord getClaimRecord(Long userId, ClaimRequest claimRequest) {
        ClaimRecord example = new ClaimRecord();
        example.setClaimName(claimRequest.getSchemaKey().getName());
        example.setClaimVersion(claimRequest.getSchemaKey().getVersion());
        example.setClaimNonce(claimRequest.getNonce());
        ClaimRecord claimRecord = claimRecordRepository
                .findOne(Example.of(example))
                .orElseThrow(() -> new IllegalArgumentException("Claim record not found."));
        Validate.validState(claimRecord.getUser().getId().equals(userId), "Claim record user mismatch.");
        return claimRecord;
    }

}