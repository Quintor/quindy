package nl.quintor.studybits.university.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@SchemaInfo(name = "Enrolment", version = "0.1")
public class Enrolment implements Claim {
    private String firstName;
    private String lastName;
    private String ssn;
    private String academicYear;

    @Override
    public String getLabel() {
        return academicYear;
    }
}
