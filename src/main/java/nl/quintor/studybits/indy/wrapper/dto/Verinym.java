package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Verinym implements Serializable, AuthCryptable {
    private String did;
    private String verkey;
    @JsonIgnore
    private String myKey;
    @JsonIgnore
    @Setter
    private String theirKey;
}
