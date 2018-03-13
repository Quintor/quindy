package nl.quintor.studybits.student.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Entity
@NoArgsConstructor
public class University {
    @Id
    private Long id;
    private String name;
    private String endpoint;
}
