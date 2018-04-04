package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.dto.ClaimRequest;
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



    public abstract String getSchemaName();

    protected abstract T getClaimForClaimRecord(ClaimRecord claimRecord);

    /**
     * Adds an available claimModel to the user.
     * Note: It only makes the claimModel available to the user without being added/written to the ledger.
     * @param userId The id of the user that will receive the claimModel.
     * @param claim The claimModel to make available.
     */
    protected void addAvailableClaim(Long userId, T claim) {
        User user = userRepository.getOne(userId);
        ClaimRecord claimRecord = new ClaimRecord(null, user, claim.getSchemaName(), claim.getSchemaVersion(), claim.getLabel(), null, null);
        claimRecordRepository.save(claimRecord);
    }

    /**
     * Retrieves the authcrypted claimModel offer after adding it to the ledger.
     * Note: The cached message will be returned if the claimModel offer was requested earlier and therefore already written
     *       to the ledger.
     * @param userId The user id.
     * @param claimRecordId The id of the ClaimRecord.
     * @return Authcrypted claimModel offer.
     */
    @SneakyThrows
    @Transactional
    public AuthcryptedMessage getClaimOffer(Long userId, Long claimRecordId) {
        ClaimRecord claimRecord = getClaimRecord(userId, claimRecordId);
        if (claimRecord.getClaimMessage() != null) {
            return toDto(claimRecord.getClaimOfferMessage());
        }
        String universityName = claimRecord.getUser().getUniversity().getName();
        Claim claim = getClaimForClaimRecord(claimRecord);
        AuthCryptableResult<ClaimOffer> result = universityService
                .createClaimOffer(universityName, claimRecord.getUser(), claim.getSchemaDefinition());
        claimRecord.setClaimOfferMessage(result.getAuthEncryptedMessage());
        claimRecordRepository.saveAndFlush(claimRecord);
        return result.getAuthcryptedMessage();
    }

    /**
     * Retrieves the authcrypted claimModel.
     * Note: The cached message will be returned if the claimModel was requested earlier and therefore already written
     *       to the ledger.
     * @param userId The user id.
     * @param authcryptedMessage The authcrypted ClaimRequest message that was provided to the client by Indy.
     * @return Authcrypted claimModel.
     */
    @SneakyThrows
    @Transactional
    public AuthcryptedMessage getClaim(Long userId, Long claimRecordId, AuthcryptedMessage authcryptedMessage) {
        ClaimRecord claimRecord = getClaimRecord(userId, claimRecordId);
        String universityName = claimRecord.getUser().getUniversity().getName();
        ClaimRequest claimRequest = universityService.authDecrypt(universityName, authcryptedMessage, ClaimRequest.class);
        if (claimRecord.getClaimMessage() != null) {
            return toDto(claimRecord.getClaimMessage());
        }
        T claim = getClaimForClaimRecord(claimRecord);
        AuthCryptableResult<nl.quintor.studybits.indy.wrapper.dto.Claim> result = universityService
                .createClaim(universityName, claimRequest, claim);
        claimRecord.setClaimOfferMessage(result.getAuthEncryptedMessage());
        claimRecordRepository.saveAndFlush(claimRecord);
        return result.getAuthcryptedMessage();
    }

    protected ClaimRecord getClaimRecord(Long userId, Long claimRecordId) {
        ClaimRecord claimRecord = claimRecordRepository
                .findById(claimRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Claim record not found."));
        Validate.validState(claimRecord.getUser().getId().equals(userId), "Claim record user mismatch.");
        Validate.validState(claimRecord.getUser().getConnection() != null, "User onboarding incomplete!");
        return claimRecord;
    }

}