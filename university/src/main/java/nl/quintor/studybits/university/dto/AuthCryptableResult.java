package nl.quintor.studybits.university.dto;

import lombok.Data;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.university.entities.AuthEncryptedMessage;

@Data
public class AuthCryptableResult<T extends AuthCryptable> {
    private final T authCryptable;
    private final AuthcryptedMessage authcryptedMessage;

    public AuthEncryptedMessage getAuthEncryptedMessage() {
        return new AuthEncryptedMessage(authcryptedMessage.getMessage(), authcryptedMessage.getDid());
    }
}