package nl.quintor.studybits.university.services;

import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.university.entities.AuthEncryptedMessage;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

@Service
public abstract class ClaimProvider<T extends nl.quintor.studybits.university.dto.Claim> {

    @Autowired
    protected IssuerService issuerService;

    @Autowired
    protected ClaimRecordRepository claimRecordRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected Mapper mapper;

    protected AuthEncryptedMessage toEntity(Object authcryptedMessage) {
        return mapper.map(authcryptedMessage, AuthEncryptedMessage.class);
    }

    protected AuthcryptedMessage toModel(Object authEncryptedMessage) {
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
        User user = userRepository.getOne(userId);
        ClaimRecord claimRecord = new ClaimRecord(null, user,  claim.getSchemaName(), claim.getSchemaVersion(), null, claim.getLabel(), null, null);
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
    public AuthcryptedMessage getClaimOffer(Long userId, Long claimRecordId) {
        ClaimRecord claimRecord = getClaimRecord(claimRecordId);
        Validate.validState(claimRecord.getUser().getId() == userId, "Claim record user mismatch.");
        User user = getConnectedUserById(userId);
        if(claimRecord.getClaimMessage() != null) {
            return toModel(claimRecord.getClaimOfferMessage());
        }
        Issuer issuer = issuerService.getIssuer(user.getUniversity().getName());
        nl.quintor.studybits.university.dto.Claim claim = getClaimForClaimRecord(claimRecord);
        ClaimOffer claimOffer = createClaimOffer(issuer, claimRecord.getUser(), claim.getSchemaDefinition());
        AuthcryptedMessage authcryptedMessage = issuer.authcrypt(claimOffer).get();
        claimRecord.setClaimNonce(claimOffer.getNonce());
        claimRecord.setClaimOfferMessage(toEntity(authcryptedMessage));
        claimRecordRepository.saveAndFlush(claimRecord);
        return authcryptedMessage;
    }

    /**
     * Retrieves the authcrypted claim.
     * Note: The cached message will be returned if the claim was requested earlier and therefore already written
     *       to the ledger.
     * @param userId The user id.
     * @param authcryptedClaimRequestMessage The authcrypted ClaimRequest message that was provided to the client by Indy.
     * @return Authcrypted claim.
     */
    @SneakyThrows
    public AuthcryptedMessage getClaim(Long userId, AuthcryptedMessage authcryptedClaimRequestMessage) {
        User user = getConnectedUserById(userId);
        Issuer issuer = issuerService.getIssuer(user.getUniversity().getName());
        ClaimRequest claimRequest = issuer.authDecrypt(authcryptedClaimRequestMessage, ClaimRequest.class).get();
        ClaimRecord claimRecord = getClaimRecord(claimRequest);
        Validate.validState(claimRecord.getUser().getId() == userId, "Claim record user mismatch.");
        if(claimRecord.getClaimMessage() != null) {
            return toModel(claimRecord.getClaimMessage());
        }
        T claim = getClaimForClaimRecord(claimRecord);
        Claim indyClaim = createClaim(issuer, claimRequest, claim);
        AuthcryptedMessage authcryptedMessage = issuer.authcrypt(indyClaim).get();
        claimRecord.setClaimOfferMessage(toEntity(authcryptedMessage));
        claimRecordRepository.saveAndFlush(claimRecord);
        return authcryptedMessage;
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
        Validate.validState(user.getId() == userId, "Claim record user mismatch.");
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