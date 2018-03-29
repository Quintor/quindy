package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userName", "university_id"}))
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String userName;

    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String ssn;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private University university;

    @OneToOne(cascade = CascadeType.ALL)
    private IndyConnection connection;

    @OneToMany(mappedBy = "user", cascade = CascadeType.MERGE, orphanRemoval = true)
    @Column(nullable = false)
    private List<ClaimRecord> claims = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private StudentUser studentUser;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AdminUser adminUser;
}