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
    private String values;
    @OneToOne
    private SchemaKey schemaKey;
    private String signature;
    private String signatureCorrectnessProof;
    private String issuerDid;
    private Integer revReqSeqNo;

    private String myDid;
    private String theirDid;
}
