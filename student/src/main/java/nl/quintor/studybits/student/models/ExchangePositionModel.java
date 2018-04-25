package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.student.enums.ExchangePositionState;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangePositionModel {
    private Boolean isOpen;
    private ExchangePositionState state;
    private Long universitySeqNo;
    private String universityName;
    private String schemaName;
    private String schemaVersion;
    private HashMap<String, String> attributes;
}
