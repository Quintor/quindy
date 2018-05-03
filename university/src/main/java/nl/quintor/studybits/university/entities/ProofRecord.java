package nl.quintor.studybits.university.entities;

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
public class ProofRecord {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String proofName;

    @Column(nullable = false)
    private String proofVersion;

    @Column(nullable = false)
    private String nonce;

    @Lob
    private String proofJson;

    @OneToOne(mappedBy = "proofRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "exchange_position_record_id", nullable = false)
    private ExchangePositionRecord exchangePositionRecord;
}