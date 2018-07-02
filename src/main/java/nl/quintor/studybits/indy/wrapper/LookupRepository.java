package nl.quintor.studybits.indy.wrapper;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.nio.file.Watchable;
import java.util.concurrent.CompletableFuture;

public interface LookupRepository {
    CompletableFuture<String> getKeyForDid(String did, Wallet wallet) throws IndyException;
    CompletableFuture<String> submitRequest(String request) throws IndyException;
    CompletableFuture<String> signAndSubmitRequest(String request, String did, Wallet wallet) throws IndyException;

}
