package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * EntitiesForClaimReferent holds the entities (schemas, claim definitions, in the future: revocation registries)
 * needed to verify the proof for that particular claim referent.
 */
public class EntitiesForClaimReferent {
    private Schema schema;
    private String claimDef;
    @JsonIgnore
    private String referent;
    // TODO: Revocation
    // private JsonNode revReg;
}
