package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    @Column( name = "user_id" )
    private Long id;

    @Column( nullable = false, unique = true )
    private String userName;

    private String firstName;

    private String lastName;

    @ManyToOne( fetch = FetchType.EAGER, optional = false )
    private University university;

    @OneToOne(cascade = CascadeType.ALL)
    private IndyConnection connection;

    @OneToMany( mappedBy = "user", cascade = CascadeType.MERGE )
    @Column( nullable = false )
    private List<ClaimRecord> claims;

    @OneToOne( cascade = CascadeType.ALL )
    @PrimaryKeyJoinColumn
    private StudentUser studentUser;

}