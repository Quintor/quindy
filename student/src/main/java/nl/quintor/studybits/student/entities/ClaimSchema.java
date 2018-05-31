package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClaimSchema {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    private University university;

    @Column(nullable = false)
    private String schemaName;

    @Column(nullable = false)
    private String schemaVersion;

    @Column(nullable = false)
    private String schemaIssuerDid;

    @ElementCollection
    private List<String> attrNames;

}
