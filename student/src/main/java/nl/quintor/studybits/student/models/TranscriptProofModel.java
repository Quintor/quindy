package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptProofModel {
    private String degree;
    private String status;
    private String average;
}