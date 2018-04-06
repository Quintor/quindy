package nl.quintor.studybits.university.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@VersionInfo(name = "Transcript", version = "0.1")
public class Transcript implements Claim {
    private String firstName;
    private String lastName;
    private String ssn;
    private String degree;
    private String status;
    private String year;
    private String average;

    @Override
    public String getLabel() {
        return degree;
    }

}