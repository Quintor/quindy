package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class WalletRecord implements Serializable {
    private String id;
    private String type;
    private String value;
    private JsonNode tags;
}
