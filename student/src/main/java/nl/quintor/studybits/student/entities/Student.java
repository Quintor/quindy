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
public class Student {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @ManyToOne
    private University originUniversity;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MetaWallet metaWallet;
}
