package nl.quintor.studybits.university.models;

import lombok.Data;

@Data
public class Student {
    private Long id;
    private String userName;
    private String firstName;
    private String lastName;
}