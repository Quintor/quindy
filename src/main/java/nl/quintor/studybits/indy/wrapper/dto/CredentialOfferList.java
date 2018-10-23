package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialOfferList implements Serializable, AuthCryptable {
    @JsonProperty("issuer_did")
    private String issuerDid;
    @JsonProperty("credential_offers")
    private List<CredentialOffer> credentialOffers;

    private String nonce;

    @JsonIgnore
    @Setter
    private String theirDid;

    public void addCredentialOffer(CredentialOffer credentialOffer) {
        this.credentialOffers.add(credentialOffer);
    }

    public List<CredentialOffer> getCredentialOffers() {
        return this.credentialOffers;
    }
}
