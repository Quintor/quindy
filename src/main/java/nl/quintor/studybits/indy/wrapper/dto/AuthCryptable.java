package nl.quintor.studybits.indy.wrapper.dto;

public interface AuthCryptable extends Serializable {
    String getTheirDid();
    String getMyDid();
    void setTheirDid(String did);
}
