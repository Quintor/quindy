package nl.quintor.studybits.indy.wrapper;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.Proof;

@Slf4j
public class Verifier extends WalletOwner {
    public Verifier(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }


    public boolean verifyProof(Proof proof) {
        return true;

    }
}
