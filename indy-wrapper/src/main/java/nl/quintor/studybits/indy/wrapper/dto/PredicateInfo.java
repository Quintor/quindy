package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class PredicateInfo implements Serializable {
    @JsonProperty("attr_name")
    private String attrName;

    @JsonProperty("p_type")
    private String pType;

    private int value;
    private Optional<List<Filter>> restrictions;

}
