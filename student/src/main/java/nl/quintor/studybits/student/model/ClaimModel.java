package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimModel {
    private Long id;
    private String ownerUserName;
    private String hashId;
    private String schemaKeyName;
    private Integer revReqSeqNo;
    private String values;
    private String signature;
    private String signatureCorrectnessProof;
    private String issuerDid;
    private String myDid;
    private String theirDid;
}
