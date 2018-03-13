package nl.quintor.studybits.student.model;

import lombok.Data;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class Student {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    @OneToOne
    private University originUniversity;
    @OneToOne
    private MetaWallet metaWallet;

    public Student(String username, University originUniversity) throws Exception {
        this.username = username;
        this.originUniversity = originUniversity;
        this.metaWallet = new MetaWallet(username);
    }

    public Prover getProver() throws Exception {
        IndyWallet indyWallet = metaWallet.getWallet();
        IndyPool indyPool = new IndyPool(PoolUtils.createPoolLedgerConfig());
        return new Prover(username, indyPool, indyWallet);
    }
}
