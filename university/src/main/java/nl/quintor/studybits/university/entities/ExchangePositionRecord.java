package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExchangePositionRecord {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne()
    @JoinColumn(name = "claimSchema_id", nullable = false)
    private ClaimSchema claimSchema;

    @Column(nullable = false)
    private Boolean isOpen;

    @Lob
    private HashMap<String, String> attributes;
}