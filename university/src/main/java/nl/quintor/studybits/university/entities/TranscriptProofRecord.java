package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TranscriptProofRecord {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String degree;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String average;

}