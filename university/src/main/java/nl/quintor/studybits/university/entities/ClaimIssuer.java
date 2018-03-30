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
public class ClaimIssuer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String did;

    @ManyToMany(mappedBy = "claimIssuers")
    private List<ClaimSchema> claimSchemas = new ArrayList<>();

    public ClaimIssuer(String name, String did) {
        this.name = name;
        this.did = did;
    }

}