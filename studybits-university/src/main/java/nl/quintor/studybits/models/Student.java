package nl.quintor.studybits.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    private Long id;
    private String userName;
    private String firstName;
    private String lastName;
}
