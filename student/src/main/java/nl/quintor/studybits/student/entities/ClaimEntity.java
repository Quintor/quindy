package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "schemaKey_id", "label"}))
public class ClaimEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(cascade = CascadeType.MERGE, optional = false)
    @JoinColumn(name = "schemaKey_id")
    private SchemaKey schemaKey;

    @Column(nullable = false)
    private String label;

    private Integer revReqSeqNo;

    @Lob
    @Column(nullable = false)
    private String values;

    @Lob
    @Column(nullable = false)
    private String signature;

    @Lob
    @Column(nullable = false)
    private String signatureCorrectnessProof;

    @Column(nullable = false)
    private String issuerDid;

    private String myDid;

    @Column(nullable = false)
    private String theirDid;


}
