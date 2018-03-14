package nl.quintor.studybits.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IndyConnection {

    @Id
    @GeneratedValue
    private Long id;

    private String did;

//    private String myDid;
//
//    private String myVerKey;
//
//    private String theirDid;
//
//    private String theirVerKey;
//
//    private String nonce;

}
