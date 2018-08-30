package nl.quintor.studybits.indy.wrapper.message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface MessageType<T> {
    public String getURN();
    public Encryption getEncryption();
    public Function<T, String> getIdProvider();
    public Class<T> getValueType();

    public enum Encryption {
        PLAINTEXT, ANONCRYPTED, AUTHCRYPTED
    }
}
