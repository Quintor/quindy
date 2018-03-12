package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Prover extends WalletOwner {
    private String masterSecretName;

    public Prover(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }

    public void init(String masterSecretName) throws IndyException, ExecutionException, InterruptedException {
        this.masterSecretName = masterSecretName;
        Anoncreds.proverCreateMasterSecret(wallet.getWallet(), masterSecretName).get();
    }

    public CompletableFuture<Void> storeClaimOffer(ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return Anoncreds.proverStoreClaimOffer(wallet.getWallet(), claimOffer.toJSON());
    }
}
