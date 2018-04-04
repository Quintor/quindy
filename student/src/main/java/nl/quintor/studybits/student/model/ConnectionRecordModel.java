package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRecordModel {
    private Long id;
    private String studentUserName;
    private String universityName;
    private String did;
    private String nonce;
    private String role;
    private String newcomerName;
    private String verkey;
}
