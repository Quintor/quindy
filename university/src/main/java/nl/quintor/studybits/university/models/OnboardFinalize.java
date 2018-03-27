package nl.quintor.studybits.university.models;

import lombok.Data;

@Data
public class OnboardFinalize {
    private byte[] message;
    private String targetDid;
}