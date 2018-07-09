package nl.quintor.studybits.indy.wrapper.dto;

import lombok.*;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class TheirDidInfo implements Serializable {
    private String did;
    private String verkey;
}
