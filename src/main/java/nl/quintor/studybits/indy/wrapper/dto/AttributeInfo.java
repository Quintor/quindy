package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class AttributeInfo implements Serializable {
    private String name;
    private Optional<List<Filter>> restrictions;
}
