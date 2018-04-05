package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"schemaKey_id", "label"}))
public class Claim implements AuthCryptable {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Student owner;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "schemaKey_id")
    private SchemaKey schemaKey;

    @Column(nullable = false)
    private String label;

    private Integer revReqSeqNo;

    @Lob
    private String values;

    @Lob
    private String signature;

    @Lob
    private String signatureCorrectnessProof;

    private String issuerDid;
    private String myDid;

    @Column(nullable = false)
    private String theirDid;


}
