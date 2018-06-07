package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapBiConsumerException;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * EntitiesFromLedger holds schemas, credentialDefs, and in the future revocation registries for a series of credential referents.
 * The Strings in the respective maps are the credential referents.
 */ public class EntitiesFromLedger {
    private Map<String, Schema> schemas;
    private Map<String, CredentialDefinition> credentialDefs;
    // TODO: Revocation
    // private Map<String, JsonNode> revRegs;

    public static Collector<EntitiesForCredentialReferent, ?, EntitiesFromLedger> collector() {
        return Collector.of(() -> new EntitiesFromLedger(new HashMap<>(), new HashMap<>()), wrapBiConsumerException(( EntitiesFromLedger entitiesFromLedger, EntitiesForCredentialReferent entitiesForCredentialReferent) -> {
            entitiesFromLedger.getSchemas()
                              .put(entitiesForCredentialReferent.getSchema().getId(), entitiesForCredentialReferent.getSchema());
            entitiesFromLedger.getCredentialDefs()
                              .put(entitiesForCredentialReferent.getCredentialDef().getId(), entitiesForCredentialReferent.getCredentialDef());
        }), ( entities1, entities2 ) -> {
            entities1.getSchemas()
                     .putAll(entities2.getSchemas());
            entities1.getCredentialDefs()
                     .putAll(entities2.getCredentialDefs());
            return entities1;
        });
    }
}
