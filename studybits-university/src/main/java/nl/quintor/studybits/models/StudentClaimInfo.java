package nl.quintor.studybits.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentClaimInfo {

    private Long id;

    private String name;

    private String version;

    private String label;

}
