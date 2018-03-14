package nl.quintor.studybits.models;

import lombok.Data;

@Data
public class OnboardFinalize {
    private byte[] message;
    private String targetDid;
}
