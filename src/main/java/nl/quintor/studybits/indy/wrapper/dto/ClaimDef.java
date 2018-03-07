package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClaimDef {
    private String ref;
    @JsonProperty("signature_type")
    private String signatureType;


}
