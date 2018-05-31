package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.dto.CredentialRequest;
import nl.quintor.studybits.university.dto.AuthCryptableResult;
import nl.quintor.studybits.university.dto.Claim;
import nl.quintor.studybits.university.entities.AuthEncryptedMessage;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public abstract class ClaimProvider<T extends Claim> {

    protected final UniversityService universityService;
    protected final ClaimRecordRepository claimRecordRepository;
    protected final UserRepository userRepository;
    protected final Mapper mapper;

    private AuthcryptedMessage toDto(AuthEncryptedMessage authEncryptedMessage) {
        return mapper.map(authEncryptedMessage, AuthcryptedMessage.class);
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
        log.debug("Adding available claim for userId: {}, claim: {}", userId, claim);
        User user = userRepository.getOne(userId);
        ClaimRecord claimRecord = new ClaimRecord(null, user, claim.getSchemaName(), claim.getSchemaVersion(), claim.getLabel(), null, null);
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
    public AuthcryptedMessage getClaimOffer(Long userId, Long claimRecordId) {
        ClaimRecord claimRecord = getClaimRecord(userId, claimRecordId);
        if (claimRecord.getClaimMessage() != null) {
            return toDto(claimRecord.getClaimOfferMessage());
        }
        String universityName = claimRecord.getUser().getUniversity().getName();
        Claim claim = getClaimForClaimRecord(claimRecord);

        AuthCryptableResult<CredentialOffer> result = universityService
                .createClaimOffer(universityName, claimRecord.getUser(), claim.getSchemaDefinition());
        claimRecord.setClaimOfferMessage(result.getAuthEncryptedMessage());
        claimRecordRepository.saveAndFlush(claimRecord);
        return result.getAuthcryptedMessage();
    }

    /**
     * Retrieves the authcrypted claim.
     * Note: The cached message will be returned if the claim was requested earlier and therefore already written
     *       to the ledger.
     * @param userId The user id.
     * @param authcryptedMessage The authcrypted ClaimRequest message that was provided to the client by Indy.
     * @return Authcrypted claim.
     */
    @SneakyThrows
    @Transactional
    public AuthcryptedMessage getClaim(Long userId, Long claimRecordId, AuthcryptedMessage authcryptedMessage) {
        ClaimRecord claimRecord = getClaimRecord(userId, claimRecordId);
        String universityName = claimRecord.getUser().getUniversity().getName();
        CredentialRequest claimRequest = universityService.authDecrypt(universityName, authcryptedMessage, CredentialRequest.class);
        if (claimRecord.getClaimMessage() != null) {
            return toDto(claimRecord.getClaimMessage());
        }
        T claim = getClaimForClaimRecord(claimRecord);
        AuthCryptableResult<nl.quintor.studybits.indy.wrapper.dto.CredentialWithRequest> result = universityService
                .createClaim(universityName, claimRequest, claim);
        claimRecord.setClaimOfferMessage(result.getAuthEncryptedMessage());
        claimRecordRepository.saveAndFlush(claimRecord);
        return result.getAuthcryptedMessage();
    }

    private ClaimRecord getClaimRecord(Long userId, Long claimRecordId) {
        ClaimRecord claimRecord = claimRecordRepository
                .findById(claimRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Claim record not found."));
        Validate.validState(claimRecord.getUser().getId().equals(userId), "Claim record user mismatch.");
        Validate.notNull(claimRecord.getUser().getConnection(), "User onboarding incomplete!");
        return claimRecord;
    }

}