package nl.quintor.studybits.student.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class Claim {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    private SchemaKey schemaKey;
    private Integer revReqSeqNo;
    private String values;
    private String signature;
    private String signatureCorrectnessProof;
    private String issuerDid;
    private String myDid;
    private String theirDid;
}
