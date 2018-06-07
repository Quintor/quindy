package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CredentialReferent implements Serializable {
    @JsonProperty("cred_info")
    private CredentialInfo credentialInfo;
    private Integer interval;
}
