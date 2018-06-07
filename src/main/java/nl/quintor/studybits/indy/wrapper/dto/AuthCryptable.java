package nl.quintor.studybits.indy.wrapper.dto;

public interface AuthCryptable extends Serializable {
    String getTheirDid();
    void setTheirDid(String did);
}
