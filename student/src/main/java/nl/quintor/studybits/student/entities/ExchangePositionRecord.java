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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"universitySeqNo", "university_id"}))
public class ExchangePositionRecord {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long universitySeqNo;

    @Column(nullable = false)
    private ExchangePositionState state;

    @ManyToOne()
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne()
    @JoinColumn(name = "claimSchema_id", nullable = false)
    private ClaimSchema claimSchema;

    @Column(nullable = false)
    private Boolean isOpen;

    @Lob
    private HashMap<String, String> attributes;
}