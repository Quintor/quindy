package nl.quintor.studybits.student.model;

import lombok.Data;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class Claim implements AuthCryptable {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    private Student owner;
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
