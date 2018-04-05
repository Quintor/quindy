package nl.quintor.studybits.university.dto;

import lombok.Data;

@Data
@VersionInfo(name = "UserProof", version = "0.1")
public class UserProof implements Proof {

    @ProofAttributeInfo(schemas = {
            @VersionInfo(name = "Enrolment", version="0.1"),
            @VersionInfo(name = "Transcript", version="0.1")
    })
    private String firstName;

    @ProofAttributeInfo(schemas = {
            @VersionInfo(name = "Enrolment", version="0.1"),
            @VersionInfo(name = "Transcript", version="0.1")
    })
    private String lastName;

    @ProofAttributeInfo(schemas = {
            @VersionInfo(name = "Enrolment", version="0.1"),
            @VersionInfo(name = "Transcript", version="0.1")
    })
    private String ssn;

}