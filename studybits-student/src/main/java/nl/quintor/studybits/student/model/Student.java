package nl.quintor.studybits.student.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Entity
@NoArgsConstructor
public class Student {
    @Id
    private Long id;
    @Setter
    private String username;
    @Setter
    private University originUniversity;
    private Prover prover;

    public Student(String username, University originUniversity) throws Exception {
        this.username = username;
        this.originUniversity = originUniversity;
        this.prover = createProver(username);
    }

    private Prover createProver(String username) throws Exception {
        IndyWallet indyWallet = new IndyWallet(username);
        IndyPool indyPool = new IndyPool(PoolUtils.createPoolLedgerConfig());
        return new Prover(username, indyPool, indyWallet);
    }
}
