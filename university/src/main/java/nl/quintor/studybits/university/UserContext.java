package nl.quintor.studybits.university;

import nl.quintor.studybits.university.models.UserIdentity;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserContext {
    private static final ThreadLocal<UserIdentity> currentUserIdentity = new ThreadLocal<>();

    @Autowired
    private UserRepository userRepository;

    public Optional<UserIdentity> getCurrentUser() {
        return Optional.ofNullable(currentUserIdentity.get());
    }

    public void setCurrentUser(String universityName, String userName) {
        if (StringUtils.isNoneBlank(universityName, userName)) {
            Optional<Long> userId = userRepository
                    .findIdByUniversityNameAndUserName(universityName, userName);
            currentUserIdentity.set(new UserIdentity(userId, universityName, userName));
        } else {
            currentUserIdentity.remove();
        }
    }

    public UserIdentity currentUserIdentity() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("UserIdentity information missing."));
    }

    public String currentUserNameName() {
        return currentUserIdentity().getUserName();
    }

    public String currentUniversityName() {
        return currentUserIdentity().getUniversityName();
    }

    public Long currentUserId() {
        return currentUserIdentity()
                .getUserId()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user."));
    }


}