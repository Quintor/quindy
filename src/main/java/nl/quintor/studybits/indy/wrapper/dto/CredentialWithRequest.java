package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialWithRequest implements Serializable, AuthCryptable {
    private Credential credential;
    private CredentialRequest credentialRequest;

    @JsonIgnore
    private String theirDid;
}
