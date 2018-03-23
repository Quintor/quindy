package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class MetaWallet {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String mainDid;
    private String mainKey;
}
