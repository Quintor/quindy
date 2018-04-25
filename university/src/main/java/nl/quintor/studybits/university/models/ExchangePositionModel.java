package nl.quintor.studybits.university.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangePositionModel {
    private Long id;
    private Boolean isOpen;
    private String universityName;
    private String schemaName;
    private String schemaVersion;
    private HashMap<String, String> attributes;
}
