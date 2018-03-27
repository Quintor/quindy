package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import org.springframework.hateoas.ResourceSupport;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class AuthEncryptedMessageModel extends ResourceSupport implements AuthCryptable {
    private byte[] message;
    private String did;

    @Override
    public String getTheirDid() {
        return did;
    }

    @Override
    public void setTheirDid(String did) {
        this.did = did;
    }
}
