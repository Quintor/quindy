package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentUser {

    @Id
    @GeneratedValue
    @Column(name = "student_user_id")
    private Long id;

    @MapsId
    @OneToOne(mappedBy = "studentUser", optional = false, orphanRemoval = true)
    @JoinColumn(name = "student_user_id")
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private Set<String> academicYears = new HashSet<>();

    @OneToMany(mappedBy = "studentUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    private List<TranscriptRecord> transcriptRecords = new ArrayList<>();

}