package nl.quintor.studybits.university.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminUser {

    @Id
    @GeneratedValue
    @Column(name = "admin_user_id")
    private Long id;

    @MapsId
    @OneToOne(mappedBy = "adminUser", optional = false)
    @JoinColumn(name = "admin_user_id")
    private User user;
}