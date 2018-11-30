package nl.quintor.studybits.indy.wrapper.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.dto.AnonCryptable;
import nl.quintor.studybits.indy.wrapper.dto.AuthCryptable;
import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.apache.commons.codec.binary.Base64;
import org.hyperledger.indy.sdk.IndyException;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public class MessageEnvelopeCodec {
    private final IndyWallet indyWallet;

    /**
     * Encrypts message to a MessageEnvelope
     *
     * @param message content as POJO
     * @param type the message type
     * @param <S> type of the content
     *
     * @return A future that resolves when the encryption is completed
     *
     * @throws JsonProcessingException
     * @throws IndyException
     */
    public <S> CompletableFuture<MessageEnvelope<S>> encryptMessage(S message, MessageType<S> type) throws JsonProcessingException, IndyException {
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
                            new TextNode(new String(Base64.encodeBase64(encryptedMessage.getMessage()), Charset.forName("UTF8")))));
        }

        return envelopeFuture;
    }

    public <S> CompletableFuture<S> decryptMessage(MessageEnvelope<S> messageEnvelope) throws IndyException, JsonProcessingException {
        if (!messageEnvelope.getMessageType().getEncryption().equals(MessageType.Encryption.PLAINTEXT) && indyWallet == null) {
            throw new IndyWrapperException("Cannot decrypt message without wallet");
        }

        String didOrNonce = messageEnvelope.getDidOrNonce();
        MessageType<S> type = messageEnvelope.getMessageType();

        CompletableFuture<S> messageFuture;
        if (type.getEncryption().equals(MessageType.Encryption.AUTHCRYPTED)) {
            messageFuture = indyWallet.authDecrypt(Base64.decodeBase64(messageEnvelope.getEncodedMessage().asText().getBytes(Charset.forName("UTF8"))), didOrNonce, type.getValueType());
        }
        else if (type.getEncryption().equals(MessageType.Encryption.ANONCRYPTED)) {
            log.debug("Anondecrypting with did: {}", didOrNonce);
            messageFuture = indyWallet.anonDecrypt(Base64.decodeBase64(messageEnvelope.getEncodedMessage().asText().getBytes(Charset.forName("UTF8"))), didOrNonce, type.getValueType());
        }
        else {
            messageFuture = CompletableFuture.completedFuture(JSONUtil.mapper.treeToValue(messageEnvelope.getEncodedMessage(), type.getValueType()));
        }

        return messageFuture;
    }
}
