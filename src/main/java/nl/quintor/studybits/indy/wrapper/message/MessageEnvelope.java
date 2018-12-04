package nl.quintor.studybits.indy.wrapper.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.dto.Serializable;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;

import java.io.IOException;

/**
 * The MessageEnvelope class is responsible for wrapping content to be sent, possibly encrypted.
 * It wraps the content in the envelope, along with the metadata to fulfill this task. The actual encryption/decryption
 * is offloaded to the {@link IndyWallet}.
 *
 * @param <T> The type of the content that is being wrapped.
 */
@Slf4j
@NoArgsConstructor
public class MessageEnvelope<T> implements Serializable {

    @Getter
    @JsonProperty("id")
    private String did;

    @JsonProperty("type")
    @Getter(value = AccessLevel.PACKAGE)
    private String type;

    @JsonProperty("message")
    @Getter(value = AccessLevel.PACKAGE)
    private JsonNode encodedMessage;


    @JsonCreator
    MessageEnvelope(@JsonProperty("id") String id, @JsonProperty("type") String type, @JsonProperty("message") JsonNode message) {
        this.type = type;
        this.did = id;
        this.encodedMessage = message;
    }

    /**
     * Returns the type of the message in this envelope
     *
     * Does a lookup based on the URN each time
     *
     * @return The type matching the URN saved. null if no such type exists
     */
    @JsonIgnore
    public MessageType<T> getMessageType() {
        return (MessageType<T>) MessageTypes.forURN(type);
    }

    /**
     * parseFromString allows parsing a message from a serialized form.
     *
     * Use this method if you are not expecting any particular message type
     *
     * @param messageEnvelope serialized envelope
     * @return deseralized envelope (still encrypted)
     * @throws IOException propagated from Jackson
     */
    public static MessageEnvelope parseFromString(String messageEnvelope) throws IOException {
        log.debug("Parsing " + messageEnvelope);
        return JSONUtil.mapper.reader().forType(MessageEnvelope.class).readValue(messageEnvelope);
    }

    /**
     * parseFromString allows parsing a message from a serialized form.
     *
     * Use this method if you are expecting a particular type
     *
     * @param messageEnvelope serialized envelope
     * @return deseralized envelope (still encrypted)
     * @throws IOException propagated from Jackson
     */
    public static <S> MessageEnvelope<S> parseFromString(String messageEnvelope, MessageType<S> messageType) throws IOException {
        log.debug("Parsing " + messageEnvelope);
        return convertEnvelope(JSONUtil.mapper.reader().forType(MessageEnvelope.class).readValue(messageEnvelope), messageType);
    }

    /**
     * convertEnvelope casts the envelope to a given type, if it is the one contained in the envelope.
     *
     * This method ensures that unchecked casts are contained within quindy.
     *
     * @param envelope Generic MessageEnvelope
     * @param messageType Expected MessageType in the envelope
     * @param <S> Type of the message contained in the envelope
     * @return The typed MessageEnvelope
     */
    public static <S> MessageEnvelope<S> convertEnvelope(MessageEnvelope envelope, MessageType<S> messageType) {
        if(!envelope.getMessageType().getValueType().equals(messageType.getValueType())) {
            throw new IllegalArgumentException("Envelope value type does not match expected value type");
        }

        return (MessageEnvelope<S>) envelope;
    }


}
