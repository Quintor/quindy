package nl.quintor.studybits.university.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

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