package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.Claim;
import nl.quintor.studybits.indy.wrapper.dto.ClaimInfo;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.dto.ClaimRequest;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.util.List;
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


    public CompletableFuture<ClaimRequest> storeClaimOfferAndCreateClaimRequest(ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return storeClaimOffer(claimOffer)
                        .thenCompose(wrapException((_void) -> createClaimRequest(claimOffer.getTheirDid(), claimOffer)));

    }

    CompletableFuture<ClaimRequest> createClaimRequest(String theirDid, ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return getPairwiseByTheirDid(theirDid)
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
                            claimRequest.setTheirDid(theirDid);
                            return claimRequest;
                        })))

                );
    }

    CompletableFuture<Void> storeClaimOffer(ClaimOffer claimOffer) throws IndyException, JsonProcessingException {
        return Anoncreds.proverStoreClaimOffer(wallet.getWallet(), claimOffer.toJSON());
    }

    public CompletableFuture<Void> storeClaim(Claim claim) throws JsonProcessingException, IndyException {
        return Anoncreds.proverStoreClaim(wallet.getWallet(), claim.toJSON(), null);
    }

    public CompletableFuture<List<ClaimInfo>> findAllClaims() throws IndyException {
        String filter = "{}";
        return Anoncreds.proverGetClaims(wallet.getWallet(), filter)
                .thenApply(this::deserializeClaimInfo);
    }

    @SneakyThrows
    private List<ClaimInfo> deserializeClaimInfo(String json) {
        return JSONUtil.mapper.readValue(json, new TypeReference<List<ClaimInfo>>(){});
    }



}
