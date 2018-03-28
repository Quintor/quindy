package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ClaimRecord {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    private Student owner;
    @OneToOne
    private Claim claim;
}
