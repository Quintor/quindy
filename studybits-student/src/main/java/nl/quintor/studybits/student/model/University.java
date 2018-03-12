package nl.quintor.studybits.student.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
public class University {
    @Id
    private Long id;
    @Getter
    private String name;
    @Getter
    private String endpoint;
}
