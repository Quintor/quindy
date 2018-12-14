package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialOfferList implements Serializable {
    @JsonProperty("issuer_did")
    private String issuerDid;
    @JsonProperty("credential_offers")
    private List<CredentialOffer> credentialOffers = new ArrayList();

    private String nonce;

    public void addCredentialOffer(CredentialOffer credentialOffer) {
        this.credentialOffers.add(credentialOffer);
    }

    public List<CredentialOffer> getCredentialOffers() {
        return this.credentialOffers;
    }
}
