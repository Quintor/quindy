package nl.quintor.studybits.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptModel {
    private String degree;
    private String status;
    private String year;
    private String average;
}
