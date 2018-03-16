package nl.quintor.studybits.models;

import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Authenticated {

    /**
     * token for authentication (should simply be userName for now)
     */
    @Getter
    private String token;
}