package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

@Data
@AllArgsConstructor
public class ProvingCredentialKey implements Serializable {
    @JsonProperty("cred_id")
    private String credId;
    private Optional<Boolean> revealed;
}
