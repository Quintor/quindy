package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Student {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;

    private String firstName;

    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private University university;

    @OneToOne
    private IndyConnection connection;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private Set<String> academicYears;

    @OneToMany(mappedBy = "student", cascade = CascadeType.MERGE)
    @Column(nullable = false)
    private List<StudentClaim> claims;

}