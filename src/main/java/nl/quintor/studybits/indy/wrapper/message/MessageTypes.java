package nl.quintor.studybits.indy.wrapper.message;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MessageTypes {
    private static final Map<String, MessageType> messageTypes = new HashMap<>();

    public static void registerType(MessageType messageType) {
        log.debug("Initializing MessageType for URN " + messageType.getURN());
        if (messageTypes.containsKey(messageType.getURN())) {
            throw new IllegalArgumentException("URN already registered: " + messageType.getURN());
        }
        messageTypes.put(messageType.getURN(), messageType);
    }

    public static MessageType forURN(String urn) {
        log.trace("Querying for URN {}", urn);
        log.trace("Message types: " + messageTypes.toString());
        return messageTypes.get(urn);
    }
}
