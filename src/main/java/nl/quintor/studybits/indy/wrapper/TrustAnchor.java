package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.dto.Verinym;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;
import static org.hyperledger.indy.sdk.did.Did.createAndStoreMyDid;
import static org.hyperledger.indy.sdk.ledger.Ledger.buildNymRequest;

@Slf4j
public class TrustAnchor extends IndyWallet {
    public TrustAnchor(IndyWallet wallet) {
        super(wallet.getName(), wallet.getMainDid(), wallet.getMainKey(), wallet.getPool(), wallet.getWallet());
        log.info("{}: Instantiated TrustAnchor: {}", name, name);
    }

    public CompletableFuture<Void> setEndpoint(String endpoint) throws IndyException {
        return Did.setEndpointForDid(getWallet(), getMainDid(), endpoint, getMainKey());
    }

    public Verinym createVerinymRequest( String targetDid ) {
        log.info("{} Creating verinym request for targetDid: {}", name, targetDid);

        return new Verinym(getMainDid(), getMainKey());
    }

    public CompletableFuture<String> acceptVerinymRequest(Verinym verinym) throws IndyException {
        log.debug("{} accepting verinym encrypted with targetDid {}", name, verinym.getDid());
       return sendNym(verinym.getDid(), verinym.getVerkey(), "TRUST_ANCHOR");
    }

    public CompletableFuture<ConnectionResponse> acceptConnectionRequest(ConnectionRequest connectionRequest) throws JsonProcessingException, IndyException {
        log.debug("{} Called acceptConnectionRequest with {}", name, connectionRequest);

        return newDid()
                .thenCompose(wrapException(result ->
                        storeDidAndPairwise(result.getDid(), connectionRequest.getDid(), connectionRequest.getVerkey())
                                .thenApply(((_str) -> new ConnectionResponse(result.getDid(), result.getVerkey())))));
    }

    CompletableFuture<String> sendNym(String newDid, String newKey, String role) throws IndyException {
        log.debug("{} Called sendNym with newDid: {}, newKey {}, role {}", name, newDid, newKey, role);
        log.debug("{} Calling buildNymRequest with mainDid: {}", name, getMainDid());
        return buildNymRequest(getMainDid(), newDid, newKey, null, role)
                .thenCompose(wrapException(this::signAndSubmitRequest));
    }
}
