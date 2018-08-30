package nl.quintor.studybits.indy.wrapper.dto;

import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.message.MessageType;

public interface AuthCryptable extends Serializable {
    String getTheirDid();
    void setTheirDid(String did);
}
