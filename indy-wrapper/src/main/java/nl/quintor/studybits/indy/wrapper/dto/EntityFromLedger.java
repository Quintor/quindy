package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityFromLedger {
    private Schema schema;
    private String claimDef;
    @JsonIgnore
    private String referent;
    // TODO: Revocation
    // private JsonNode revRegs;
}
