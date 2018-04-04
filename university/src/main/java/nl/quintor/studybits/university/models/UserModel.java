package nl.quintor.studybits.university.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
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