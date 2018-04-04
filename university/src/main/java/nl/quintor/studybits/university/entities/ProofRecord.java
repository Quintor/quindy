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

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String proofName;

    @Column(nullable = false)
    private String proofVersion;

    @Column(nullable = false)
    private String nonce;

    /**
     * Used to cache the encrypted proof message.
     */
    @Embedded
    private AuthEncryptedMessage proofMessage;

}