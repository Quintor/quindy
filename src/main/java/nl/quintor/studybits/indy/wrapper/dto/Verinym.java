package nl.quintor.studybits.indy.wrapper.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Verinym implements Serializable {
    private String did;
    private String verkey;
}
