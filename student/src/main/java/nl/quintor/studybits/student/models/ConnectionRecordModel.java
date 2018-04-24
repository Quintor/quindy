package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRecordModel {
    private Long id;
    private String did;
    private String nonce;
    private String role;
    private Boolean confirmed;
    private String studentUserName;
    private String universityName;
}
