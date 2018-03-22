package nl.quintor.studybits.entities;


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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "claimName", "claimVersion", "claimLabel"}))
public class StudentClaim {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Student student;

    @Column(nullable = false)
    private String claimName;

    @Column(nullable = false)
    private String claimVersion;

    private String claimNonce;

    @Column(nullable = false)
    private String claimLabel;

    /**
     * Used to cache the encrypted ClaimOffer message.
     */
    @AttributeOverrides({
            @AttributeOverride(name="message",column=@Column(name="claimOfferMessage")),
            @AttributeOverride(name="did",column=@Column(name="claimOfferDid"))
            })
    @Embedded
    private AuthEncryptedMessage claimOfferMessage;

    /**
     * Used to cache the encrypted Claim message.
     */
    @AttributeOverrides({
            @AttributeOverride(name="message",column=@Column(name="claimMessage")),
            @AttributeOverride(name="did",column=@Column(name="claimDid"))
    })
    @Embedded
    private AuthEncryptedMessage claimMessage;
}