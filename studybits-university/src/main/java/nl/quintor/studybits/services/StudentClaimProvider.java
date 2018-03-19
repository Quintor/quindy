package nl.quintor.studybits.services;

import nl.quintor.studybits.entities.AuthEncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimRequest;
import nl.quintor.studybits.models.StudentClaimInfo;

import java.util.List;

public interface StudentClaimProvider {

    List<StudentClaimInfo> findAvailableClaims(String universityName, String userName);

    boolean claimOfferExists(Long studentClaimId);

    AuthEncryptedMessage createClaimOffer(String universityName, String userName, Long studentClaimId);

    AuthEncryptedMessage getClaimOffer(String universityName, String userName, Long studentClaimId);

    boolean claimExists(Long studentClaimId);

    AuthEncryptedMessage createClaim(String universityName, String userName, ClaimRequest claimRequest);

    AuthEncryptedMessage getClaim(String universityName, String userName, ClaimRequest claimRequest);
}