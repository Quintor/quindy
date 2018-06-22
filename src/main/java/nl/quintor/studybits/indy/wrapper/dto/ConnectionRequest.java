package nl.quintor.studybits.indy.wrapper.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionRequest implements Serializable {
    private String did;
    @JsonProperty("request_nonce")
    private String requestNonce;
    @JsonIgnore
    private String role;
    @JsonIgnore
    private String newcomerName;
    @JsonIgnore
    private String verkey;
}
