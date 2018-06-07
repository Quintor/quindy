package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialRequest implements Serializable, AuthCryptable {
    private String request;
    private String metadata;
    private CredentialOffer credentialOffer;

    @JsonIgnore
    private String theirDid;
}
