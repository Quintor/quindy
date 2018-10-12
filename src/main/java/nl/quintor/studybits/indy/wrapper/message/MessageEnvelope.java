package nl.quintor.studybits.indy.wrapper.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.dto.AnonCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.Serializable;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.apache.commons.codec.binary.Base64;
import org.hyperledger.indy.sdk.IndyException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    public MessageEnvelope(@JsonProperty("id") String id, @JsonProperty("type") String type, @JsonProperty("message") JsonNode message) {
        this.type = type;
        this.didOrNonce = id;
        this.encodedMessage = message;
    }

    public static <S> CompletableFuture<MessageEnvelope<S>> fromMessage(S message, MessageType<S> type, IndyWallet indyWallet) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
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
            envelopeFuture = encryptedMessageFuture.thenApply(encryptedMessage -> new MessageEnvelope<S>(encryptedMessage.getTargetDid(), type.getURN(),  new TextNode(Base64.encodeBase64String(encryptedMessage.getMessage()))));
        }

        return envelopeFuture;
    }

    public CompletableFuture<T> getMessage(IndyWallet indyWallet) throws IndyException, JsonProcessingException {
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

    @JsonIgnore
    public MessageType<T> getMessageType() {
        return MessageTypes.forURN(type);
    }

    public static <S> MessageEnvelope<S> parseFromString(String messageEnvelope) throws IOException {
        log.debug("Parsing " + messageEnvelope);
        return JSONUtil.mapper.reader().forType(MessageEnvelope.class).readValue(messageEnvelope);
    }


}
