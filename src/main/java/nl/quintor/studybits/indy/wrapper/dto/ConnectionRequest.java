package nl.quintor.studybits.indy.wrapper.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionRequest implements Serializable {
    private String did;
    private String nonce;
    @JsonIgnore
    private String role;
    @JsonIgnore
    private String newcomerName;
    @JsonIgnore
    private String verkey;
}
