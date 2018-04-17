package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProofRequestRecord {
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
    private String link;

    @Column(nullable = false)
    private Long proofId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @ElementCollection
    private List<String> attributes;

    @Column(nullable = false)
    private Boolean isReviewed;

}
