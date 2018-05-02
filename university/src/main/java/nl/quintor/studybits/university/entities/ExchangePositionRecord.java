package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.quintor.studybits.university.enums.ExchangePositionState;

import javax.persistence.*;
import java.util.HashMap;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "schema_definition_record_id"}))
public class ExchangePositionRecord {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "university_id")
    private University university;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "schema_definition_record_id", nullable = false)
    private SchemaDefinitionRecord schemaDefinitionRecord;

    @OneToOne(mappedBy = "exchangePositionRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "proof_record_id", nullable = false)
    private ProofRecord proofRecord;

    @Column(nullable = false)
    private ExchangePositionState state;

    @Lob
    private HashMap<String, String> attributes;
}