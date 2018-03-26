package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_user_id", "degree"}))
public class TranscriptRecord {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "student_user_id", nullable = false)
    private StudentUser studentUser;

    @Column(nullable = false)
    private String degree;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String year;

    @Column(nullable = false)
    private String average;
}