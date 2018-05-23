package nl.quintor.studybits.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.quintor.studybits.enums.ExchangePositionState;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class ExchangePositionModel {
    private String universityName;
    private SchemaDefinitionModel schemaDefinitionModel;
    private Long proofRecordId;
    private ExchangePositionState state;
    private HashMap<String, String> attributes;
}
