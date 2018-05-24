package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.quintor.studybits.student.enums.ExchangeApplicationState;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExchangeApplicationRecord {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "university_id")
    private University university;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private Student student;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "exchange_position_record_id")
    private ExchangePositionRecord exchangePositionRecord;

    @Column(nullable = false)
    private ExchangeApplicationState state;

    @OneToOne(mappedBy = "exchangeApplicationRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private TranscriptProofRecord proof;
}