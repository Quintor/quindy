package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthcryptedMessage {
    private byte[] message;
    private String did;
}
