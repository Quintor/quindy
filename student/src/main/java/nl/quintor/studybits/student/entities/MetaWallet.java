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
public class MetaWallet {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.PERSIST)
    private Student student;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String mainDid;

    @Column(nullable = false, unique = true)
    private String mainKey;
}
