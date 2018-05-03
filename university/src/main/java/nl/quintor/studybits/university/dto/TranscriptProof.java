package nl.quintor.studybits.university.dto;

import lombok.Data;

@Data
@VersionInfo(name = "TranscriptProof", version = "0.1")
public class TranscriptProof implements Proof {

    @ProofAttributeInfo(schemas = {
            @VersionInfo(name = "Transcript", version="0.1")
    })
    private String degree;

    @ProofAttributeInfo(schemas = {
            @VersionInfo(name = "Transcript", version="0.1")
    })
    private String status;

    @ProofAttributeInfo(schemas = {
            @VersionInfo(name = "Transcript", version="0.1")
    })
    private String average;

}