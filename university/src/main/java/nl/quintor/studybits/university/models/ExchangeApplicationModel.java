package nl.quintor.studybits.university.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.university.enums.ExchangeApplicationState;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeApplicationModel {
    private String universityName;
    private String userName;
    private ExchangePositionModel exchangePositionModel;
    private ExchangeApplicationState state;
    private TranscriptProofModel proof;
}
