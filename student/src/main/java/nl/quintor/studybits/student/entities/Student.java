package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userName", "originUniversity_id"}))
public class Student {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String ssn;

    @ManyToOne
    @JoinColumn(name = "originUniversity_id")
    private University originUniversity;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MetaWallet metaWallet;
}
