package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionRecord {
    @Id
    @GeneratedValue
    private Long id;

    private Student student;
    private University university;

    private String did;
    private String nonce;
    private String role;
    private String newcomerName;
    private String verkey;
}
