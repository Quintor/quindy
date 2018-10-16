package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.AnonCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.Serializable;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import nl.quintor.studybits.indy.wrapper.message.MessageType;
import nl.quintor.studybits.indy.wrapper.message.MessageTypes;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.apache.commons.codec.binary.Base64;
import org.hyperledger.indy.sdk.IndyException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

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
    private String didOrNonce;

    @JsonProperty("type")
    private String type;

    @JsonProperty("message")
    private JsonNode encodedMessage;


    @JsonCreator
    private MessageEnvelope(@JsonProperty("id") String id, @JsonProperty("type") String type, @JsonProperty("message") JsonNode message) {
        this.type = type;
        this.didOrNonce = id;
        this.encodedMessage = message;
    }

    /**
     * Encrypts message to a MessageEnvelope
     *
     * @param message content as POJO
     * @param type the message type
     * @param indyWallet used to encrypt, optional if the type has encryption plaintext
     * @param <S> type of the content
     *
     * @return A future that resolves when the encryption is completed
     *
     * @throws JsonProcessingException
     * @throws IndyException
     */
    public static <S> CompletableFuture<MessageEnvelope<S>> encryptMessage(S message, MessageType<S> type, IndyWallet indyWallet) throws JsonProcessingException, IndyException {
        if (!type.getEncryption().equals(MessageType.Encryption.PLAINTEXT) && indyWallet == null) {
            throw new IndyWrapperException("Cannot encrypt message without wallet");
        }

        log.debug("Creating MessageEnvelope from message {}", message);


        CompletableFuture<EncryptedMessage> encryptedMessageFuture = null;
        if (type.getEncryption().equals(MessageType.Encryption.AUTHCRYPTED)) {
            String did = ((AuthCryptable) message).getTheirDid();
            encryptedMessageFuture = indyWallet.authEncrypt(JSONUtil.mapper.writeValueAsBytes(message), did);
        }
        else if (type.getEncryption().equals(MessageType.Encryption.ANONCRYPTED)) {
            String did = ((AnonCryptable) message).getTheirDid();
            encryptedMessageFuture = indyWallet.anonEncrypt(JSONUtil.mapper.writeValueAsBytes(message), did);
        }

        CompletableFuture<MessageEnvelope<S>> envelopeFuture;

        if (encryptedMessageFuture == null) {
            envelopeFuture = CompletableFuture.<JsonNode>completedFuture(JSONUtil.mapper.valueToTree(message))
                    .thenApply(encodedMessage -> new MessageEnvelope<S>(type.getIdProvider().apply(message), type.getURN(), encodedMessage));
        }
        else {
            envelopeFuture = encryptedMessageFuture.thenApply(encryptedMessage ->
                    new MessageEnvelope<>(encryptedMessage.getTargetDid(), type.getURN(),
                            new TextNode(Base64.encodeBase64String(encryptedMessage.getMessage()))));
        }

        return envelopeFuture;
    }

    public CompletableFuture<T> extractMessage(IndyWallet indyWallet) throws IndyException, JsonProcessingException {
        if (!getMessageType().getEncryption().equals(MessageType.Encryption.PLAINTEXT) && indyWallet == null) {
            throw new IndyWrapperException("Cannot decrypt message without wallet");
        }

        CompletableFuture<T> messageFuture;
        if (getMessageType().getEncryption().equals(MessageType.Encryption.AUTHCRYPTED)) {
            messageFuture = indyWallet.authDecrypt(Base64.decodeBase64(encodedMessage.asText()), didOrNonce, getMessageType().getValueType());
        }
        else if (getMessageType().getEncryption().equals(MessageType.Encryption.ANONCRYPTED)) {
            messageFuture = indyWallet.anonDecrypt(Base64.decodeBase64(encodedMessage.asText()), didOrNonce, getMessageType().getValueType());
        }
        else {
            messageFuture = CompletableFuture.completedFuture(JSONUtil.mapper.treeToValue(encodedMessage, getMessageType().getValueType()));
        }

        return messageFuture;
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
