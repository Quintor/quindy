package nl.quintor.studybits.student.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class SchemaKey {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String version;
    private String did;
}
