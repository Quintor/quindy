package nl.quintor.studybits.indy.wrapper;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.dto.Verinym;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import org.hyperledger.indy.sdk.IndyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;
import static org.hyperledger.indy.sdk.did.Did.createAndStoreMyDid;
import static org.hyperledger.indy.sdk.ledger.Ledger.buildNymRequest;

@Slf4j
public class TrustAnchor extends WalletOwner {
    private Map<String, ConnectionRequest> openConnectionRequests = new HashMap<>();
    private Map<String, String> rolesByDid = new HashMap<>();

    public TrustAnchor(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
        log.info("{}: Instantiated TrustAnchor: {}", name, name);
    }

    public CompletableFuture<ConnectionRequest> createConnectionRequest(String newcomerName, String role) throws IndyException {
        log.info("'{}' -> Create and store in Wallet '{} {}'", name, name, newcomerName);
        return createAndStoreMyDid(wallet.getWallet(), "{}")
                .thenCompose(wrapException(
                        (didResult) ->
                                sendNym(didResult.getDid(), didResult.getVerkey(), role)
                                        .thenApply(
                                        // TODO: Generate nonce properly
                                        (nymResponse) -> {
                                            ConnectionRequest connectionRequest = new ConnectionRequest(didResult.getDid(), Long.toString(System.currentTimeMillis()), role, newcomerName, didResult.getVerkey());
                                            log.debug("Returning ConnectionRequest: {}", connectionRequest);
                                            openConnectionRequests.put(connectionRequest.getNonce(), connectionRequest);
                                            return connectionRequest;
                                        }
                                )
                        )
                );
    }

    public Verinym createVerinymRequest( String targetDid ) {
        log.info("{} Creating verinym request for targetDid: {}", name, targetDid);

        return new Verinym(wallet.getMainDid(), wallet.getMainKey(), targetDid);
    }

    public CompletableFuture<String> acceptVerinymRequest(Verinym verinym) throws IndyException {
        log.debug("{} accepting verinym encrypted with did {}", name, verinym.getDid());
       return sendNym(verinym.getDid(), verinym.getVerkey(), rolesByDid.get(verinym.getTheirDid()));
    }

    public CompletableFuture<String> acceptConnectionResponse(ConnectionResponse connectionResponse) throws IndyException {
        if (!openConnectionRequests.containsKey(connectionResponse.getNonce())) {
            log.info("No open connection request for nonce {}", connectionResponse.getNonce());
            throw new IndyWrapperException("Nonce not found");
        }

        ConnectionRequest connectionRequest = openConnectionRequests.get(connectionResponse.getNonce());

        return sendNym(connectionResponse.getDid(), connectionResponse.getVerkey(), connectionRequest.getRole())
                .thenCompose(wrapException((nymResponse) ->
                        storeDidAndPairwise(connectionRequest.getDid(), connectionResponse.getDid(), connectionResponse.getVerkey())))
                .thenApply((void_) -> {
                    log.debug("Removing connectionRequest with nonce {}", connectionRequest.getNonce());
                    rolesByDid.put(connectionResponse.getDid(), connectionRequest.getRole());
                    openConnectionRequests.remove(connectionRequest.getNonce());
                    return connectionResponse.getDid();
                });
    }

    CompletableFuture<String> sendNym(String newDid, String newKey, String role) throws IndyException {
        log.debug("{} Called sendNym with newDid: {}, newKey {}, role {}", name, newDid, newKey, role);
        log.debug("{} Calling buildNymRequest with mainDid: {}", name, wallet.getMainDid());
        return buildNymRequest(wallet.getMainDid(), newDid, newKey, null, role)
                .thenCompose(wrapException(this::signAndSubmitRequest));
    }
}
