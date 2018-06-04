package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "schemaName", "schemaVersion"}))
public class ClaimSchema {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "university_id")
    private University university;

    @Column(nullable = false)
    private String schemaName;

    @Column(nullable = false)
    private String schemaVersion;

    @Column(nullable = false)
    private String schemaIssuerDid;

    @Column(nullable = false)
    private Boolean claimDefined;

    @ElementCollection
    private List<String> attrNames;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "known_claim_issuers",
            joinColumns = @JoinColumn(name = "claimschema_id"),
            inverseJoinColumns = @JoinColumn(name = "claimissuer_id")
    )
    private List<ClaimIssuer> claimIssuers = new ArrayList<>();

    public ClaimSchema(University university, String schemaName, String schemaVersion, String schemaIssuerDid) {
        this.university = university;
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
        this.schemaIssuerDid = schemaIssuerDid;
        this.claimDefined = false;
    }
}
