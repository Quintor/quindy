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
public class University {

    @Id
    @GeneratedValue
    @Column(name = "university_id")
    private Long id;

    @OneToOne(mappedBy = "university", orphanRemoval = true)
    private User user;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Column(nullable = false)
    private List<ClaimSchema> claimSchemas = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    private List<ExchangePositionRecord> exchangePositionRecords = new ArrayList<>();

}