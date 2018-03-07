package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.Schema;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class Issuer extends TrustAnchor {
    @Getter
    private String issuerDid;
    @Getter
    private String issuerKey;

    public Issuer(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }

    public static Issuer create(String name, IndyPool pool, IndyWallet wallet) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        Issuer issuer = new Issuer(name, pool, wallet);
        DidResults.CreateAndStoreMyDidResult issuerDidAndKey = wallet.newDid()
                .thenCompose(wrapException(createAndStoreMyDidResult ->
                        issuer.sendNym(createAndStoreMyDidResult.getDid(), createAndStoreMyDidResult.getVerkey(), null)
                        .thenApply(sendNymResult -> createAndStoreMyDidResult))).get();
        issuer.issuerDid = issuerDidAndKey.getDid();
        issuer.issuerKey = issuerDidAndKey.getVerkey();

        return issuer;
    }

    public CompletableFuture<SchemaKey> createAndSendSchema(String name, String version, String... attrNames) throws IndyException, JsonProcessingException {
        Schema schema = new Schema(name, version, Arrays.asList(attrNames));
        SchemaKey schemaKey = SchemaKey.fromSchema(schema, issuerDid);

        log.debug("{}: Creating schema: {}", name, schema.toJSON());

        return Ledger.buildSchemaRequest(issuerDid, schema.toJSON())
                .thenCompose(wrapException(this::signAndSubmitRequest))
                .thenApply(requestResponse -> schemaKey);
    }
}
