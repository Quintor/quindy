package nl.quintor.studybits.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@SchemaInfo(name = "Enrolment", version = "0.1")
public class Enrolment {
    private String academicYear;
}
