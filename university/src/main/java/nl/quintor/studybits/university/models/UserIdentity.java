package nl.quintor.studybits.university.models;

import lombok.Data;

@Data
public class UserIdentity {

    private final Long userId;

    private final String universityName;

    private final String userName;
}
