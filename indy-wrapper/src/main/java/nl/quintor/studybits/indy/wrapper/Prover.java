package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.dto.Schema;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class Prover extends WalletOwner {
    private String masterSecretName;

    public Prover(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }

    public void init(String masterSecretName) throws IndyException, ExecutionException, InterruptedException {
        this.masterSecretName = masterSecretName;
        Anoncreds.proverCreateMasterSecret(wallet.getWallet(), masterSecretName).get();
    }

    public CompletableFuture<String> storeClaimOfferAndCreateClaimRequest(AuthcryptedMessage authcryptedClaimoffer) throws IndyException {
        return authDecrypt(authcryptedClaimoffer, ClaimOffer.class)
                .thenCompose(wrapException(claimOffer -> storeClaimOffer(claimOffer)
                        .thenCompose(wrapException((_void) -> proveClaimOffer(authcryptedClaimoffer.getDid(), claimOffer)))
                ));

    }

    CompletableFuture<String> proveClaimOffer(String theirDid, ClaimOffer claimOffer) throws IndyException {
        return getPairwiseByTheirDid(theirDid)
                .thenCompose(wrapException(pairwiseResult -> getSchema(pairwiseResult.getMyDid(), claimOffer.getSchemaKey())
                                .thenCompose(wrapException(schema -> getClaimDef(pairwiseResult.getMyDid(), schema, claimOffer.getIssuerDid())))
                                .thenCompose(wrapException(claimDef -> {
                                    log.debug("{} creating claim request with claimDef {}", name, claimDef);
                                    return Anoncreds.proverCreateAndStoreClaimReq(wallet.getWallet(), pairwiseResult.getMyDid(),
                                            claimOffer.toJSON(), claimDef, this.masterSecretName);
                                }))
                                .thenApply(claimReq -> {
                                    log.debug(claimReq);
                                    return claimReq;
                                })
                        )
                );
    }

    CompletableFuture<Void> storeClaimOffer(ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return Anoncreds.proverStoreClaimOffer(wallet.getWallet(), claimOffer.toJSON());
    }
}
