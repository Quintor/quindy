package nl.quintor.studybits.indy.wrapper.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.quintor.studybits.indy.wrapper.dto.Serializable;

@Data
@AllArgsConstructor
public class MessageEnvelope implements Serializable {
    public static final String TYPE_PREFIX = "urn:indy:sov:agent:message_type:sovrin.org/connection/1.0/";

    private String id;
    private MessageType type;
    private JsonNode message;

    public enum MessageType {
        CONNECTION_OFFER("connection_offer"), CONNECTION_REQUEST("connection_request"),
        CONNECTION_RESPONSE("connection_response"), CONNECTION_ACKNOWLEDGEMENT("connection_acknowledgement"),
        CREDENTIAL_OFFER("credential_offer"), CREDENTIAL_REQUEST("credential_request"), CREDENTIAL("credential"),
        PROOF_REQUEST("proof_request"), PROOF("proof");

        private String type;

        MessageType(String type) {
            this.type = type;
        }

        @JsonCreator
        public static MessageType forValue(String value) {
            if (!value.startsWith(TYPE_PREFIX)) {
                throw new IllegalArgumentException("Type does not start with the needed prefix");
            }

            String type = value.replaceFirst(TYPE_PREFIX, "");

            return MessageType.valueOf(type.toUpperCase());
        }

        @JsonValue
        public String getType() {
            return TYPE_PREFIX + this.type;
        }
    }
}
