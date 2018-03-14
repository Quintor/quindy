package nl.quintor.studybits.models;

import lombok.Data;

@Data
public class OnboardBegin {
    private final String did;
    private final String nonce;
}
