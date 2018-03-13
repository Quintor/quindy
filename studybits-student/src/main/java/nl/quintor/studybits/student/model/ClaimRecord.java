package nl.quintor.studybits.student.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class ClaimRecord {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    private final Student owner;
    @OneToOne
    private final Claim claim;
}
