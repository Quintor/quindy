package nl.quintor.studybits.university.dto;

import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;

import java.util.Map;

public interface Claim {

    default String getSchemaName() {
        return ClaimUtils.getSchemaName(getClass());
    }

    default String getSchemaVersion() {
        return ClaimUtils.getSchemaVersion(getClass());
    }

    default SchemaDefinition getSchemaDefinition() {
        return ClaimUtils.getSchemaDefinition(getClass());
    }

    default Map<String, Object> toMap() {
        return ClaimUtils.getMapOfClaim(this);
    }

    String getLabel();
}