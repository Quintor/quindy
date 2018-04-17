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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "university_id"}))
public class ConnectionRecord {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(cascade = CascadeType.MERGE, optional = false)
    @JoinColumn(name = "university_id")
    private University university;

    @Column(nullable = false)
    private String did;

    @Column(nullable = false)
    private String nonce;

    @Column(nullable = false)
    private Boolean confirmed;

    private String role;
}