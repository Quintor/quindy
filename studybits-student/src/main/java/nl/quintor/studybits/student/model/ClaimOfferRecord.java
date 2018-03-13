package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@AllArgsConstructor
public class ClaimOfferRecord {
    @Id
    private Long id;
    private Student owner;
    private ClaimOffer offer;
}
