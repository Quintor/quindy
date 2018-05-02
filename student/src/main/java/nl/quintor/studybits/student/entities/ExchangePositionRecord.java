package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.quintor.studybits.student.enums.ExchangePositionState;

import javax.persistence.*;
import java.util.HashMap;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"proofRecordId", "university_id"}))
public class ExchangePositionRecord {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "schema_definition_record_id", nullable = false)
    private SchemaDefinitionRecord schemaDefinitionRecord;

    @Column(nullable = false)
    private Long proofRecordId;

    @Column(nullable = false)
    private ExchangePositionState state;

    @Lob
    private HashMap<String, String> attributes;
}