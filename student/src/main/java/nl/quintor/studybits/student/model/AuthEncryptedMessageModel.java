package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class AuthEncryptedMessageModel extends ResourceSupport {
    private byte[] message;
    private String did;

//    @JsonProperty("_links")
//    private Map<String, Map<String, String>> links;
}
