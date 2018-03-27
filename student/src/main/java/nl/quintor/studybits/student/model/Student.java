package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    @ManyToOne
    private University originUniversity;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MetaWallet metaWallet;
}
