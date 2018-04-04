package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapBiConsumerException;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * EntitiesFromLedger holds schemas, claimDefs, and in the future revocation registries for a series of claimModel referents.
 * The Strings in the respective maps are the claimModel referents.
 */ public class EntitiesFromLedger {
    private Map<String, Schema> schemas;
    private Map<String, JsonNode> claimDefs;
    // TODO: Revocation
    // private Map<String, JsonNode> revRegs;

    public static Collector<EntitiesForClaimReferent, ?, EntitiesFromLedger> collector() {
        return Collector.of(() -> new EntitiesFromLedger(new HashMap<>(), new HashMap<>()), wrapBiConsumerException(( EntitiesFromLedger entitiesFromLedger, EntitiesForClaimReferent entitiesForClaimReferent ) -> {
            entitiesFromLedger.getSchemas()
                              .put(entitiesForClaimReferent.getReferent(), entitiesForClaimReferent.getSchema());
            entitiesFromLedger.getClaimDefs()
                              .put(entitiesForClaimReferent.getReferent(), JSONUtil.mapper.readValue(entitiesForClaimReferent.getClaimDef(), JsonNode.class));
        }), ( entities1, entities2 ) -> {
            entities1.getSchemas()
                     .putAll(entities2.getSchemas());
            entities1.getClaimDefs()
                     .putAll(entities2.getClaimDefs());
            return entities1;
        });
    }
}
