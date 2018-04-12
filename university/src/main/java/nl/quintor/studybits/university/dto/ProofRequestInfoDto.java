package nl.quintor.studybits.university.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProofRequestInfoDto {
    private Long proofId;

    private String name;

    private String version;

    private List<String> attributes;
}
