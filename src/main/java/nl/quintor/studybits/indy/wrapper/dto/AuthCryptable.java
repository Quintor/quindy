package nl.quintor.studybits.indy.wrapper.dto;

public interface AuthCryptable extends Serializable {
    String getTheirKey();
    String getMyKey();
    void setTheirKey(String key);
}
