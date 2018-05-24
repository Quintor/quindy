package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TranscriptProofRecord {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    private ExchangeApplicationRecord exchangeApplicationRecord;

    @Column(nullable = false)
    private String degree;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String average;
}