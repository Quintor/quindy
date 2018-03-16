package nl.quintor.studybits.student.model;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Data
@Entity
public class SchemaDefinition {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String version;
    @ElementCollection
    private List<String> attrNames;
}
