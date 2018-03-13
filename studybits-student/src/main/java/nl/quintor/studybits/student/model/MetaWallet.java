package nl.quintor.studybits.student.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyWallet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@Slf4j
public class MetaWallet {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String mainDid;
    private String mainKey;

    public MetaWallet(String name) {
        this.name = name;
        try {
            IndyWallet indyWallet = new IndyWallet(name);
            this.mainDid = indyWallet.getMainDid();
            this.mainKey = indyWallet.getMainKey();
        } catch (Exception e) {
            log.warn("Exception thrown during initialisation of IndyWallet.");
            log.warn(e.toString());
        }
    }

    public IndyWallet getWallet() throws Exception {
        return new IndyWallet(name, mainDid, mainKey);
    }
}
