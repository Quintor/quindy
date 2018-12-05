package nl.quintor.studybits.indy.wrapper.message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface MessageType<T> {
    public String getURN();
    public Encryption getEncryption();
    public Class<T> getValueType();

    public enum Encryption {
        ANONCRYPTED, AUTHCRYPTED
    }
}
