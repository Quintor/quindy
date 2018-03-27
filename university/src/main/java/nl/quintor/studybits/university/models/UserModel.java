package nl.quintor.studybits.university.models;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserModel {
    private Long id;

    @NotNull(message = "can't be missing")
    @Size(min = 1, message = "can't be empty")
    private String userName;

    @NotNull(message = "can't be missing")
    @Size(min = 1, message = "can't be empty")
    private String firstName;

    @NotNull(message = "can't be missing")
    @Size(min = 1, message = "can't be empty")
    private String lastName;

    @NotNull(message = "can't be missing")
    @Size(min = 1, message = "can't be empty")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "must be a valid ssn (XXX-XX-XXXX)")
    private String ssn;
}