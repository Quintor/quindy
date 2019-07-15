package nl.quintor.studybits.indy.wrapper.message;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.*;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class IndyMessageTypes {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final String SOVRIN_URN_PREFIX = "urn:indy:sov:agent:message_type:sovrin.org/";

    public static final MessageType<ConnectionRequest> CONNECTION_REQUEST = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "connection/1.0/connection_request", MessageType.Encryption.ANONCRYPTED, ConnectionRequest.class);

    public static final MessageType<ConnectionResponse> CONNECTION_RESPONSE = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "connection/1.0/connection_response", MessageType.Encryption.ANONCRYPTED, ConnectionResponse.class);

    public static final MessageType<Verinym> VERINYM = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "connection/1.0/verinym", MessageType.Encryption.AUTHCRYPTED, Verinym.class);

    public static final MessageType<AuthcryptableString> CONNECTION_ACKNOWLEDGEMENT = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "connection/1.0/connection_acknowledgement", MessageType.Encryption.AUTHCRYPTED, AuthcryptableString.class);

    public static final MessageType<CredentialOffer> CREDENTIAL_OFFER = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "credential/1.0/credential_offer", MessageType.Encryption.AUTHCRYPTED, CredentialOffer.class);

    public static final MessageType<CredentialOfferList> CREDENTIAL_OFFERS = new StandardMessageType<> (
            SOVRIN_URN_PREFIX + "credential/1.0/credential_offers", MessageType.Encryption.AUTHCRYPTED, CredentialOfferList.class);

    public static final MessageType<CredentialRequest> CREDENTIAL_REQUEST = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "credential/1.0/credential_request", MessageType.Encryption.AUTHCRYPTED, CredentialRequest.class);

    public static MessageType<Credential> CREDENTIAL = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "credential/1.0/credential", MessageType.Encryption.AUTHCRYPTED, Credential.class);

    public static final MessageType<ProofRequest> PROOF_REQUEST = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "proof/1.0/proof_request", MessageType.Encryption.AUTHCRYPTED, ProofRequest.class);

    public static final MessageType<Proof> PROOF = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "proof/1.0/proof", MessageType.Encryption.AUTHCRYPTED, Proof.class);

    public static final MessageType<String> GET_REQUEST = new StandardMessageType<>(
            SOVRIN_URN_PREFIX + "get/1.0/get_request", MessageType.Encryption.AUTHCRYPTED, String.class);

    static {
        init();
    }

    public static void init() {
        log.debug("Trying to initialize message types");
        if (!initialized.get()) {
            if(initialized.compareAndSet(false, true)) {
                log.debug("Initializing message types");
                MessageTypes.registerType(CONNECTION_REQUEST);
                MessageTypes.registerType(CONNECTION_RESPONSE);
                MessageTypes.registerType(VERINYM);
                MessageTypes.registerType(CONNECTION_ACKNOWLEDGEMENT);
                MessageTypes.registerType(CREDENTIAL_OFFER);
                MessageTypes.registerType(CREDENTIAL_OFFERS);
                MessageTypes.registerType(CREDENTIAL_REQUEST);
                MessageTypes.registerType(CREDENTIAL);
                MessageTypes.registerType(PROOF_REQUEST);
                MessageTypes.registerType(PROOF);
                MessageTypes.registerType(GET_REQUEST);
            }
        }
    }
    @Data
    public static class StandardMessageType<T> implements MessageType<T> {
        private final String URN;
        private final Encryption encryption;
        private final Class<T> valueType;

        public StandardMessageType(String URN, Encryption encryption, Class<T> valueType) {
            this.URN = URN;
            this.encryption = encryption;
            this.valueType = valueType;
        }
    }
}
