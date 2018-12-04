package nl.quintor.studybits.indy.wrapper.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Data;
import lombok.NonNull;
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
    @NonNull
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
        if (indyWallet == null) {
            throw new IndyWrapperException("Cannot encrypt message without wallet");
        }

        log.debug("Creating MessageEnvelope from message {}", message);


        CompletableFuture<EncryptedMessage> encryptedMessageFuture = null;
        switch (type.getEncryption()) {
            case AUTHCRYPTED: {
                String did = ((AuthCryptable) message).getTheirDid();
                encryptedMessageFuture = indyWallet.authEncrypt(JSONUtil.mapper.writeValueAsBytes(message), did);
                break;
            }
            case ANONCRYPTED: {
                String did = ((AnonCryptable) message).getTheirDid();
                encryptedMessageFuture = indyWallet.anonEncrypt(JSONUtil.mapper.writeValueAsBytes(message), did);
                break;
            }
            default: throw new IllegalArgumentException("Unsupported encryption type");
        }

        return encryptedMessageFuture.thenApply(encryptedMessage ->
            new MessageEnvelope<>(encryptedMessage.getTargetDid(), type.getURN(),
            new TextNode(new String(Base64.encodeBase64(encryptedMessage.getMessage()), Charset.forName("UTF8")))));
    }

    public <S> CompletableFuture<S> decryptMessage(MessageEnvelope<S> messageEnvelope) throws IndyException, JsonProcessingException {
        if (indyWallet == null) {
            throw new IndyWrapperException("Cannot decrypt message without wallet");
        }

        String didOrNonce = messageEnvelope.getDid();
        MessageType<S> type = messageEnvelope.getMessageType();

        CompletableFuture<S> messageFuture;

        switch (type.getEncryption()) {
            case AUTHCRYPTED: {
                messageFuture = indyWallet.authDecrypt(Base64.decodeBase64(messageEnvelope.getEncodedMessage().asText().getBytes(Charset.forName("UTF8"))), didOrNonce, type.getValueType());
                break;
            }
            case ANONCRYPTED: {
                log.debug("Anondecrypting with did: {}", didOrNonce);
                messageFuture = indyWallet.anonDecrypt(Base64.decodeBase64(messageEnvelope.getEncodedMessage().asText().getBytes(Charset.forName("UTF8"))), didOrNonce, type.getValueType());

                break;
            }
            default: throw new IllegalArgumentException("Unsupported encryption type");
        }

        return messageFuture;
    }
}
