package nl.quintor.studybits.student.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRecord {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Student student;
    @OneToOne
    private University university;

    private String did;
    private String nonce;
    private String role;
    private String newcomerName;
    private String verkey;
}
