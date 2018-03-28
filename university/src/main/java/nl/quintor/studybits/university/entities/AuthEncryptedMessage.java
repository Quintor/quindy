package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class AuthEncryptedMessage {
    @Lob
    private byte[] message;
    private String did;
}
