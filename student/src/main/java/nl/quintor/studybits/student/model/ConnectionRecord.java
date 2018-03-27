package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
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
