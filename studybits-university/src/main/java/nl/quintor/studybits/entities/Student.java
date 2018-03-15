package nl.quintor.studybits.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Student {

    @Id
    @GeneratedValue
    private Long id;

    private String userName;

    private String firstName;

    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    private University university;

    @OneToOne
    private IndyConnection connection;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private Set<String> academicYears;
}