package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialWithRequest implements Serializable {
    private Credential credential;
    private CredentialRequest credentialRequest;
}
