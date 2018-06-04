package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.quintor.studybits.university.enums.ExchangeApplicationState;

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
    private User user;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "exchange_position_record_id")
    private ExchangePositionRecord exchangePositionRecord;

    @Column(nullable = false)
    private ExchangeApplicationState state;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "proof_id")
    private TranscriptProofRecord proof;
}