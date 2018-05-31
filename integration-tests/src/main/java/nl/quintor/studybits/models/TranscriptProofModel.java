package nl.quintor.studybits.models;

import lombok.Data;

@Data
public class TranscriptProofModel {
    private Long id;
    private String degree;
    private String status;
    private String average;
}
