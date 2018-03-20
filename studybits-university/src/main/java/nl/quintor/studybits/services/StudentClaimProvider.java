package nl.quintor.studybits.services;

import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.models.StudentClaimInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class StudentClaimProvider {

    public String getClaimProviderId() {
        return StringUtils.removeEndIgnoreCase(getClass().getName(), "service");
    }

    public abstract List<StudentClaimInfo> findAvailableClaims(String universityName, String userName);

    public abstract boolean claimOfferExists(Long studentClaimId);

    public abstract AuthcryptedMessage createClaimOffer(String universityName, String userName, Long studentClaimId);

    public abstract AuthcryptedMessage getClaimOffer(String universityName, String userName, Long studentClaimId);

    public abstract boolean claimExists(Long studentClaimId);

    public abstract AuthcryptedMessage createClaim(String universityName, String userName, AuthcryptedMessage authcryptedClaimRequestMessage);

    public abstract AuthcryptedMessage getClaim(String universityName, String userName, AuthcryptedMessage authcryptedClaimRequestMessage);
}