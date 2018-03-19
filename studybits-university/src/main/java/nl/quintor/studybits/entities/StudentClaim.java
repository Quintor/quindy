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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student", "claimName", "claimVersion", "claimLabel"}))
public class StudentClaim {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private Student student;

    @Column(nullable = false)
    private String claimName;

    @Column(nullable = false)
    private String claimVersion;

    @Column(nullable = false)
    private String claimNonce;

    @Column(nullable = false)
    private String claimLabel;

    @Embedded
    private AuthEncryptedMessage claimOfferMessage;

    @Embedded
    private AuthEncryptedMessage claimMessage;
}