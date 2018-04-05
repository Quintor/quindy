package nl.quintor.studybits.student.models;

import lombok.Data;

@Data
public class ClaimModel {
    private Long id;
    private String hashId;
    private Integer revReqSeqNo;
    private String values;
    private String signature;
    private String signatureCorrectnessProof;
    private String issuerDid;
    private String myDid;
    private String theirDid;
}
