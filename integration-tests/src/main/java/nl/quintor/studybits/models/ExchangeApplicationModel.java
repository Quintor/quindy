package nl.quintor.studybits.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.enums.ExchangeApplicationState;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeApplicationModel {
    private Long id;
    private String universityName;
    private String userName;
    private ExchangePositionModel exchangePositionModel;
    private ExchangeApplicationState state;
    private TranscriptProofModel proof;
}
