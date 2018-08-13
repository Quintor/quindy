package nl.quintor.studybits.indy.wrapper.message;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.apache.commons.codec.binary.Base64;
import org.hyperledger.indy.sdk.IndyException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@JsonSerialize(using = MessageEnvelope.Serializer.class)
@Slf4j
public class MessageEnvelope<T> implements Serializable {
    @Getter
    private MessageType<T> type;
    private T message;

    private JsonNode encodedMessage;

    @Setter
    private IndyWallet indyWallet;
    @Getter
    private String theirDid;

    @JsonCreator
    public MessageEnvelope(@JsonProperty("id") String id, @JsonProperty("type") String type, @JsonProperty("message") JsonNode message) {
        this.type = MessageTypes.forURN(type);
        this.theirDid = id;
        this.encodedMessage = message;
    }

    public T getMessage() throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        if (message != null) {
            return message;
        }

        if (this.type.getEncryption().equals(MessageType.Encryption.AUTHCRYPTED)) {
            this.message = indyWallet.authDecrypt(Base64.decodeBase64(encodedMessage.asText()), theirDid, this.type.getValueType()).get();
        }
        else if (this.type.getEncryption().equals(MessageType.Encryption.ANONCRYPTED)) {
            this.message = indyWallet.anonDecrypt(Base64.decodeBase64(encodedMessage.asText()), theirDid, this.type.getValueType()).get();
        }
        else {
            this.message = JSONUtil.mapper.treeToValue(encodedMessage, this.type.getValueType());
        }

        return this.message;
    }

    public static class Serializer extends StdSerializer<MessageEnvelope> {
        public Serializer() {
            this(null);
        }

        public Serializer(Class<MessageEnvelope> t) {
            super(t);
        }

        @Override
        public void serialize(MessageEnvelope m, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("type", m.type.getURN());

            try {
                if (m.type.getEncryption().equals(MessageType.Encryption.AUTHCRYPTED)) {
                    AuthcryptedMessage authcryptedMessage = m.indyWallet.authEncrypt(JSONUtil.mapper.writeValueAsBytes(m.message), m.theirDid).get();
                    jsonGenerator.writeStringField("id", authcryptedMessage.getDid());
                    jsonGenerator.writeStringField("message", Base64.encodeBase64String(authcryptedMessage.getMessage()));
                } else if (m.type.getEncryption().equals(MessageType.Encryption.ANONCRYPTED)) {
                    byte[] bytes = JSONUtil.mapper.writeValueAsBytes(m.message);
                    AnoncryptedMessage anoncryptedMessage = m.indyWallet.anonEncrypt(bytes, m.theirDid).get();
                    jsonGenerator.writeStringField("id", anoncryptedMessage.getTargetDid());
                    jsonGenerator.writeStringField("message", Base64.encodeBase64String(anoncryptedMessage.getMessage()));
                } else {
                    jsonGenerator.writeStringField("id", (String) m.type.getIdProvider().apply(m.message));
                    if (m.message != null) {
                        jsonGenerator.writeObjectField("message", m.message);
                    }
                }
            }
            catch (IndyException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new IOException("Exception during encryption", e);
            }

            jsonGenerator.writeEndObject();
        }
    }

    public static <S extends AuthCryptable> MessageEnvelope<S> fromAuthcryptable(S message, MessageType<S> messageType, IndyWallet indyWallet) {
        return new MessageEnvelope<S>(messageType, message, null, indyWallet, message.getTheirDid());
    }

    public static <S extends AnonCryptable> MessageEnvelope<S> fromAnoncryptable(S message, MessageType<S> messageType, IndyWallet indyWallet) {
        return new MessageEnvelope<S>(messageType, message, null, indyWallet, message.getTheirDid());
    }

    public static <S> MessageEnvelope<S> parseFromString(String messageEnvelope, IndyWallet indyWallet) throws IOException {
        MessageEnvelope<S> messageEnvelopeObject =  JSONUtil.mapper.reader().forType(MessageEnvelope.class).readValue(messageEnvelope);
        messageEnvelopeObject.setIndyWallet(indyWallet);
        return messageEnvelopeObject;
    }
}
