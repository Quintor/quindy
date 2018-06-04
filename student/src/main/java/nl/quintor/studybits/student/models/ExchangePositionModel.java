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
    private String universityName;
    private String schemaId;
    private Long proofRecordId;
    private ExchangePositionState state;
    private HashMap<String, String> attributes;
}
