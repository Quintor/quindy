package nl.quintor.studybits.university.models;

import lombok.Data;
import java.util.Optional;

@Data
public class UserIdentity {

    private final Optional<Long> userId;

    private final String universityName;

    private final String userName;
}
