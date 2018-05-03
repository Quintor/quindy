package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.student.dto.TranscriptProof;
import nl.quintor.studybits.student.enums.ExchangeApplicationState;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeApplicationModel {
    private String universityName;
    private String userName;
    private ExchangePositionModel exchangePositionModel;
    private ExchangeApplicationState state;
    private TranscriptProof proof;
}
