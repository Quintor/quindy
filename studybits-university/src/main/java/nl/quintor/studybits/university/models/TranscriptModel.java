package nl.quintor.studybits.university.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranscriptModel {

    private String degree;
    private String status;
    private String year;
    private String average;
}
