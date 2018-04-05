package nl.quintor.studybits.university.dto;

public interface Versioned {
    default String getSchemaName() {
        return ClaimUtils.getVersion(getClass()).getName();
    }

    default String getSchemaVersion() {
        return ClaimUtils.getVersion(getClass()).getVersion();
    }
}
