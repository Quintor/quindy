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
public class EntitiesFromLedger {
    private Map<String, Schema> schemas;
    private Map<String, JsonNode> claimDefs;
    // TODO: Revocation
    // private JsonNode revRegs;

    public static Collector<EntityFromLedger, ?, EntitiesFromLedger> collector() {
        return Collector.of(() -> new EntitiesFromLedger(new HashMap<>(), new HashMap<>()),
                wrapBiConsumerException((EntitiesFromLedger entitiesFromLedger, EntityFromLedger entityFromLedger) -> {
                    entitiesFromLedger.getSchemas().put(entityFromLedger.getReferent(), entityFromLedger.getSchema());
                        entitiesFromLedger.getClaimDefs().put(entityFromLedger.getReferent(), JSONUtil.mapper.readValue(entityFromLedger.getClaimDef(), JsonNode.class));
                }), (entities1, entities2) -> {
                    entities1.getSchemas().putAll(entities2.getSchemas());
                    entities1.getClaimDefs().putAll(entities2.getClaimDefs());
                    return entities1;
                }
                );
    }
}
