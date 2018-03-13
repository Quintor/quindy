package nl.quintor.studybits.models;

import lombok.Data;

@Data
public class OnboardBeginResponse {
    private final String did;
    private final String nonce;
}
