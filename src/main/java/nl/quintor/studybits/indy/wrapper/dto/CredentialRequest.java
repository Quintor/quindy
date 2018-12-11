package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialRequest implements Serializable {
    private String request;
    private String metadata;
    private CredentialOffer credentialOffer;
}
