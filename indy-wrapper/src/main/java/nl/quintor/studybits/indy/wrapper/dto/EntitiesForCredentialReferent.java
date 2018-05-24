package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * EntitiesForCredentialReferent holds the entities (schemas, credential definitions, in the future: revocation registries)
 * needed to verify the proof for that particular credential referent.
 */ public class EntitiesForCredentialReferent {
    private Schema schema;
    private CredentialDefinition credentialDef;
    @JsonIgnore
    private String referent;
    // TODO: Revocation
    // private JsonNode revReg;
}
