package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Verinym implements Serializable, AuthCryptable {
    private String did;
    private String verkey;

    @JsonIgnore
    @Setter
    private String theirDid;
}
