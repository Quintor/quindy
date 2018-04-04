package nl.quintor.studybits.university.dto;

import lombok.Data;

@Data
public class EnrolmentProof implements Proof {

    @ProofAttributeInfo(schemas = { @SchemaInfo(name = "Enrolment", version="0.1") })
    private String firstName;

    @ProofAttributeInfo(schemas = { @SchemaInfo(name = "Enrolment", version="0.1") })
    private String lastName;

    @ProofAttributeInfo(schemas = { @SchemaInfo(name = "Enrolment", version="0.1") })
    private String ssn;

    @ProofAttributeInfo(schemas = { @SchemaInfo(name = "Enrolment", version="0.1") })
    String academicYear;
}
