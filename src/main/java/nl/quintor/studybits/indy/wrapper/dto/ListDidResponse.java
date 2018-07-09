package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ListDidResponse implements Serializable {
    private String did;
    private String verkey;
    private JsonNode metadata;
}
