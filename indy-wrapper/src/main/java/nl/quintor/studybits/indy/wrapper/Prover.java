package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.dto.ClaimRequest;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
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

    public CompletableFuture<AuthcryptedMessage> storeClaimOfferAndCreateClaimRequest(AuthcryptedMessage authcryptedClaimoffer) throws IndyException {
        return authDecrypt(authcryptedClaimoffer, ClaimOffer.class)
                .thenCompose(wrapException(claimOffer -> storeClaimOffer(claimOffer)
                        .thenCompose(wrapException((_void) -> createClaimRequest(authcryptedClaimoffer.getDid(), claimOffer)))
                ));

    }

    CompletableFuture<AuthcryptedMessage> createClaimRequest(String theirDid, ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return authcrypt(getPairwiseByTheirDid(theirDid)
                .thenCompose(wrapException(pairwiseResult -> getSchema(pairwiseResult.getMyDid(), claimOffer.getSchemaKey())
                                .thenCompose(wrapException(schema -> getClaimDef(pairwiseResult.getMyDid(), schema, claimOffer.getIssuerDid())))
                                .thenCompose(wrapException(claimDefJson -> {
                                    log.debug("{} creating claim request with claimDefJson {}", name, claimDefJson);
                                    return Anoncreds.proverCreateAndStoreClaimReq(wallet.getWallet(), pairwiseResult.getMyDid(),
                                            claimOffer.toJSON(), claimDefJson, this.masterSecretName)
                                            .thenCompose(wrapException(claimReqJsonStorageResponse -> {
                                                log.debug("{} Got claim request storage response {}", name, claimReqJsonStorageResponse);
                                                return Anoncreds.proverCreateAndStoreClaimReq(wallet.getWallet(), pairwiseResult.getMyDid(),
                                                        claimOffer.toJSON(), claimDefJson, this.masterSecretName);
                                            }));
                                })).thenApply(wrapException(claimRequestJson -> {
                                    ClaimRequest claimRequest = JSONUtil.mapper.readValue(claimRequestJson, ClaimRequest.class);
                                    claimRequest.setMyDid(pairwiseResult.getMyDid());
                                    claimRequest.setTheirDid(theirDid);
                                    return claimRequest;
                                })))
                        )
                );
    }

    CompletableFuture<Void> storeClaimOffer(ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return Anoncreds.proverStoreClaimOffer(wallet.getWallet(), claimOffer.toJSON());
    }
}
