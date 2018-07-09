package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionResponse implements AnonCryptable, Serializable {
    private String did;
    private String verkey;
    @JsonProperty("request_nonce")
    private String requestNonce;
    @JsonIgnore
    private String theirDid;
}
