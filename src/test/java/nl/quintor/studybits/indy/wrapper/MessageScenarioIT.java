package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.TestUtil.removeIndyClientDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class MessageScenarioIT {

    /*
        Steps refer to the steps in this document: https://github.com/hyperledger/indy-sdk/blob/rc/doc/getting-started/getting-started.md
     */

    @Test
    public void fullScenarioTest() throws Exception {
        // Clear indy_client directory
        removeIndyClientDirectory();

        // #Step 2
        // Set pool protocol version based on PoolUtils
        Pool.setProtocolVersion(PoolUtils.PROTOCOL_VERSION).get();

        // Initialize message types for MessageEnvelope
        IndyMessageTypes.init();

        // Create and open Pool with 'default_pool' as pool name
        String poolName = PoolUtils.createPoolLedgerConfig(null);
        IndyPool indyPool = new IndyPool(poolName);

        // #Step 3
        // Create new wallet (with DID and VerKey) using seed for Steward
        TrustAnchor steward = new TrustAnchor(IndyWallet.create(indyPool, "steward_wallet", "000000000000000000000000Steward1"));

        // Create new wallet (with DID and VerKey) for Government (TrustAnchor)
        Issuer government = new Issuer(IndyWallet.create(indyPool, "government_wallet",null));
        // #Step 4
        // Onboard the issuers (onboard -> verinym -> issuerDids)
        // Onboard the TrustAnchor
        onboardIssuer(steward, government);

        // #Step 4.1.7 & 4.1.8
        // Create new wallet (with DID and VerKey) for Faber (TrustAnchor)
        Issuer faber = new Issuer(IndyWallet.create(indyPool, "faber_wallet", null));
        // Onboard the TrustAnchor
        onboardIssuer(steward, faber);

        // Create new wallet (with DID and VerKey) for ACME (TrustAnchor)
        IndyWallet acmeWallet = IndyWallet.create(indyPool, "acme_wallet", null);
        Issuer acme = new Issuer(acmeWallet);
        // Onboard the TrustAnchor
        onboardIssuer(steward, acme);

        // Create new wallet (with DID and VerKey) for Thrift (TrustAnchor)
        Issuer thriftWallet = new Issuer(IndyWallet.create(indyPool, "thrift_wallet", null));
        Issuer thrift = new Issuer(thriftWallet);
        // Onboard the TrustAnchor
        onboardIssuer(steward, thrift);

        // Create new wallet (with DID and VerKey) for Alice (IdentityOwner)
        Prover alice = new Prover(IndyWallet.create(indyPool, "alice_wallet", null), "alice_master_secret");
        // Onboard alice to Faber. Creates connection request with Faber
        String aliceFaberDid = onboardWalletOwner(faber, alice);
        // Create master secret for alice (Prover / Identity owner)
        alice.init();
        // Onboard alice to ACME. Creates connection request with ACME
        String aliceAcmeDid = onboardWalletOwner(acme, alice);
        // Onboard alice to Thrift. Creates connection request with Thrift
        String aliceThriftDid = onboardWalletOwner(thrift, alice);

        // #Step 5.1 & 5.2
        // Government creates schemas for Job certificate
        String jobCertificateSchemaId = government.createAndSendSchema("Job-Certificate", "0.2",
                "first_name", "last_name", "salary", "employee_status", "experience").get();
        // Government creates schemas for Transcript
        String transcriptSchemaId = government.createAndSendSchema("Transcript", "1.2",
                "first_name", "last_name", "degree", "status", "year", "average", "ssn").get();

        // #Step 6.1 & 6.2 & 6.3
        // Faber requests the scheme from the ledger
        // then create a scheme definition
        // then sends the CredDef transaction to the ledger.
        String transcriptCredentialDefId = faber.defineCredential(transcriptSchemaId).get();

        // ACME requests the scheme from the ledger
        String jobCertificateCredentialDefId = acme.defineCredential(jobCertificateSchemaId).get();

        // ---------------------Alice Gets a Transcript---------------------
        // Faber creates a credential offer for Alice
        CredentialOffer transcriptCredentialOffer = faber.createCredentialOffer(transcriptCredentialDefId, aliceFaberDid).get();
        String credentialOfferMessageEnvelope = MessageEnvelope.fromAuthcryptable(transcriptCredentialOffer, IndyMessageTypes.CREDENTIAL_OFFER, faber).toJSON();

        // Alice receives the transcript offer
        CredentialOffer decryptedTranscriptCredentialOffer = MessageEnvelope.<CredentialOffer>parseFromString(credentialOfferMessageEnvelope, alice).getMessage();
        // Alice creates a credential request
        CredentialRequest transcriptCredentialRequest = alice.createCredentialRequest(decryptedTranscriptCredentialOffer).get();
        String transcriptCredentialRequestMessageEnvelope = MessageEnvelope.<CredentialRequest>fromAuthcryptable(transcriptCredentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST, alice).toJSON();


        Map<String, Object> credentialValues  = new HashMap<>();
        credentialValues.put("first_name", "Alice");
        credentialValues.put("last_name", "Garcia");
        credentialValues.put("degree", "Bachelor of Science, Marketing");
        credentialValues.put("status", "graduated");
        credentialValues.put("ssn", "123-45-6789");
        credentialValues.put("year", 2015);
        credentialValues.put("average", 5);

        // Faber receives the credential request from Alice
        CredentialRequest credentialRequest = MessageEnvelope.<CredentialRequest>parseFromString(transcriptCredentialRequestMessageEnvelope, faber).getMessage();
        // Faber creates a new credential for Alice and sends it to her
        CredentialWithRequest credential = faber.createCredential(credentialRequest, credentialValues).get();
        String credentialMessageEnvelope = MessageEnvelope.fromAuthcryptable(credential, IndyMessageTypes.CREDENTIAL, faber).toJSON();

        // Alice receives her credential and stores it inside her wallet
        CredentialWithRequest decryptedCredential = MessageEnvelope.<CredentialWithRequest>parseFromString(credentialMessageEnvelope, alice).getMessage();
        alice.storeCredential(decryptedCredential).get();

        // ---------------------Apply for a Job---------------------
        // DEBUG: Check credentials
        List<CredentialInfo> credentialInfos = alice.findAllCredentials()
                .get();
        System.out.println("CREDENTIAL INFOS:");
        System.out.println(credentialInfos);

        // ACME Builds the proof request
        List<Filter> transcriptFilter = Collections.singletonList(new Filter(transcriptCredentialDefId));
        ProofRequest jobApplicationProofRequest = ProofRequest.builder()
                .name("Job-Application")
                .nonce("1432422343242122312411212")
                .version("0.1")
                .requestedAttribute("attr1_referent", new AttributeInfo("first_name", Optional.empty()))
                .requestedAttribute("attr2_referent", new AttributeInfo("last_name", Optional.empty()))
                .requestedAttribute("attr3_referent", new AttributeInfo("degree", Optional.of(transcriptFilter)))
                .requestedAttribute("attr4_referent", new AttributeInfo("status", Optional.of(transcriptFilter)))
                .requestedAttribute("attr5_referent", new AttributeInfo("ssn", Optional.of(transcriptFilter)))
                .requestedAttribute("attr6_referent", new AttributeInfo("phone_number", Optional.empty()))
                .requestedPredicate("predicate1_referent", new PredicateInfo("average", ">=", 4, Optional.of(transcriptFilter)))
                .build();

        jobApplicationProofRequest.setTheirDid(aliceAcmeDid);
        // ACME Sends proof request to Alice
        String jobApplicationProofRequestEnvelope = MessageEnvelope.fromAuthcryptable(jobApplicationProofRequest, IndyMessageTypes.PROOF_REQUEST, acme).toJSON();


        Map<String, String> selfAttestedAttributes = new HashMap<>();
        selfAttestedAttributes.put("first_name", "Alice");
        selfAttestedAttributes.put("last_name", "Garcia");
        selfAttestedAttributes.put("phone_number", "123phonenumber");

        // Alice receives proof request from ACME
        ProofRequest decryptedProofRequest = MessageEnvelope.<ProofRequest>parseFromString(jobApplicationProofRequestEnvelope, alice).getMessage();
        // Alice fufills proof request
        // Create proof object from available credentials for this proof request and self-attested attributes
        Proof proof = alice.fulfillProofRequest(decryptedProofRequest, selfAttestedAttributes).get();
        // Send proof to ACME
        String proofEnvelope = MessageEnvelope.fromAuthcryptable(proof, IndyMessageTypes.PROOF, alice).toJSON();

        // ACME receives proof
        Proof decryptedProof = MessageEnvelope.<Proof>parseFromString(proofEnvelope, acme).getMessage();

        List<ProofAttribute> attributes = new Verifier(acmeWallet).getVerifiedProofAttributes(jobApplicationProofRequest, decryptedProof).get();

        System.out.println(attributes);
        // ACME Validates proof
        assertThat(attributes, containsInAnyOrder(
                new ProofAttribute("attr1_referent", "first_name", "Alice"),
                new ProofAttribute("attr2_referent", "last_name", "Garcia"),
                new ProofAttribute("attr3_referent", "degree", "Bachelor of Science, Marketing"),
                new ProofAttribute("attr4_referent", "status", "graduated"),
                new ProofAttribute("attr5_referent", "ssn", "123-45-6789"),
                new ProofAttribute("attr6_referent", "phone_number", "123phonenumber")
                ));

        // ACME Validates proof
        boolean isValidProof = new Verifier(acmeWallet).validateProof(jobApplicationProofRequest, decryptedProof).get();
        System.out.println("Degree validation: " + isValidProof);

        Assert.assertTrue(isValidProof);

        // ACME creates a new credential offer (Job certificate) for Alice
        // ACME creates a credential offer for Alice
        CredentialOffer jobCertificateCredentialOffer = acme.createCredentialOffer(jobCertificateCredentialDefId, aliceAcmeDid).get();
        credentialOfferMessageEnvelope = MessageEnvelope.fromAuthcryptable(jobCertificateCredentialOffer, IndyMessageTypes.CREDENTIAL_OFFER, acme).toJSON();


        // Alice receives the jobCertificate offer
        CredentialOffer decryptedJobCertificateCredentialOffer = MessageEnvelope.<CredentialOffer>parseFromString(credentialOfferMessageEnvelope, alice).getMessage();
        // Alice creates a credential request
        CredentialRequest jobCertificateCredentialRequest = alice.createCredentialRequest(decryptedJobCertificateCredentialOffer).get();
        String jobCertificateCredentialRequestMessageEnvelope = MessageEnvelope.<CredentialRequest>fromAuthcryptable(jobCertificateCredentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST, alice).toJSON();


        Map<String, Object> jobCredentialValues  = new HashMap<>();
        jobCredentialValues.put("first_name", "Alice");
        jobCredentialValues.put("last_name", "Garcia");
        jobCredentialValues.put("salary", 3000);
        jobCredentialValues.put("employee_status", "Permanent");
        jobCredentialValues.put("experience", 5);

        // ACME receives the credential request from Alice
        CredentialRequest jobCredentialRequest = MessageEnvelope.<CredentialRequest>parseFromString(jobCertificateCredentialRequestMessageEnvelope, acme).getMessage();
        // ACME creates a new credential for Alice and sends it to her
        CredentialWithRequest jobCredential = acme.createCredential(jobCredentialRequest, jobCredentialValues).get();
        String jobCredentialMessageEnvelope = MessageEnvelope.fromAuthcryptable(jobCredential, IndyMessageTypes.CREDENTIAL, acme).toJSON();

        // Alice receives her credential and stores it inside her wallet
        CredentialWithRequest decryptedJobCredential = MessageEnvelope.<CredentialWithRequest>parseFromString(jobCredentialMessageEnvelope, alice).getMessage();
        alice.storeCredential(decryptedJobCredential).get();


        // DEBUG: Check credentials again
        credentialInfos  = alice.findAllCredentials()
                .get();

        System.out.println("CREDENTIAL INFOS:");
        System.out.println(credentialInfos);

        // ---------------------Apply for a loan---------------------
        // Alice now has a proof of being an employee at ACME (Job credential in her wallet) and should be able to apply for a loan

        // Thrift builds the proof request
        List<Filter> jobCertificateFilter = Collections.singletonList(new Filter(jobCertificateCredentialDefId));
        ProofRequest loanApplicationProofRequest = ProofRequest.builder()
                .name("Loan-Application-Basic")
                .nonce("1432422343242122312411212")
                .version("0.1")
                .requestedAttribute("attr1_referent", new AttributeInfo("employee_status", Optional.of(jobCertificateFilter)))
                .requestedPredicate("predicate1_referent", new PredicateInfo("salary", ">=", 2000, Optional.of(jobCertificateFilter)))
                .requestedPredicate("predicate2_referent", new PredicateInfo("experience", ">=", 1, Optional.of(jobCertificateFilter)))
                .build();

        loanApplicationProofRequest.setTheirDid(aliceThriftDid);
        // Thrift Sends proof request to Alice
        String loanApplicationProofRequestEnvelope = MessageEnvelope.fromAuthcryptable(loanApplicationProofRequest, IndyMessageTypes.PROOF_REQUEST, thrift).toJSON();


        Map<String, String> emptyMap = new HashMap<>();

        // Alice receives proof request from Thrift
        decryptedProofRequest = MessageEnvelope.<ProofRequest>parseFromString(loanApplicationProofRequestEnvelope, alice).getMessage();
        // Alice fufills proof request
        // Create proof object from available credentials for this proof request and self-attested attributes
        Proof jobProof = alice.fulfillProofRequest(decryptedProofRequest, emptyMap).get();
        // Send proof to Thrift
        String jobProofEnvelope = MessageEnvelope.fromAuthcryptable(jobProof, IndyMessageTypes.PROOF, alice).toJSON();

        // Thrift receives proof
        Proof decryptedJobProof = MessageEnvelope.<Proof>parseFromString(jobProofEnvelope, thrift).getMessage();

        List<ProofAttribute> jobAttributes = new Verifier(thriftWallet).getVerifiedProofAttributes(loanApplicationProofRequest, decryptedJobProof).get();

        System.out.println(jobAttributes);
        // Thrift Validates proof
        assertThat(jobAttributes, containsInAnyOrder(
                new ProofAttribute("attr1_referent", "employee_status", "Permanent")
        ));

        // Thrift Validates proof
        Boolean jobIsValidProof = new Verifier(thriftWallet).validateProof(loanApplicationProofRequest, decryptedJobProof).get();
        System.out.println("Job validation: " + jobIsValidProof);

        Assert.assertTrue(jobIsValidProof);

        // If proof is valid then thrift will request a new proof request asking Alice's personal information
        ProofRequest loanApplicationPersonalDetailsProofRequest = ProofRequest.builder().build();
        if(jobIsValidProof) {
            loanApplicationPersonalDetailsProofRequest = ProofRequest.builder()
                    .name("Loan-Application-KYC")
                    .nonce("123432421212")
                    .version("0.1")
                    .requestedAttribute("attr1_referent", new AttributeInfo("first_name", Optional.empty()))
                    .requestedAttribute("attr2_referent", new AttributeInfo("last_name", Optional.empty()))
                    .requestedAttribute("attr3_referent", new AttributeInfo("ssn", Optional.empty()))
                    .build();

            loanApplicationPersonalDetailsProofRequest.setTheirDid(aliceThriftDid);

        }

        // Thrift Sends proof request to Alice
        String loanApplicationPersonalDetailsProofRequestEnvelope = MessageEnvelope.fromAuthcryptable(loanApplicationPersonalDetailsProofRequest, IndyMessageTypes.PROOF_REQUEST, thrift).toJSON();


        // Alice receives proof request from Thrift
        decryptedProofRequest = MessageEnvelope.<ProofRequest>parseFromString(loanApplicationPersonalDetailsProofRequestEnvelope, alice).getMessage();

        // Alice fufills proof request
        // Create proof object from available credentials for this proof request and self-attested attributes
        Proof KYCProof = alice.fulfillProofRequest(decryptedProofRequest, emptyMap).get();
        // Send proof to Thrift
        String KYCProofEnvelope = MessageEnvelope.fromAuthcryptable(KYCProof, IndyMessageTypes.PROOF, alice).toJSON();

        // Thrift receives proof
        Proof decryptedKYCProof = MessageEnvelope.<Proof>parseFromString(KYCProofEnvelope, thrift).getMessage();

        List<ProofAttribute> KYCAttributes = new Verifier(thriftWallet).getVerifiedProofAttributes(loanApplicationPersonalDetailsProofRequest, decryptedKYCProof).get();

        System.out.println(KYCAttributes);

        // Thrift Validates proof
        Boolean KYCIsValidProof = new Verifier(thriftWallet).validateProof(loanApplicationProofRequest, decryptedJobProof).get();
        System.out.println("KYC Validation: " + KYCIsValidProof);

        Assert.assertTrue(KYCIsValidProof);
    }

    public static void onboardIssuer(TrustAnchor steward, Issuer newcomer) throws InterruptedException, ExecutionException, IndyException, IOException {
        // Connecting newcomer with Steward

        // #Step 4.1.2 & 4.1.3
        // Create new DID (For steward_faber connection) and send NYM request to ledger
        String governmentConnectionRequest = steward.createConnectionRequest(newcomer.getName(), "TRUST_ANCHOR")
                .thenApply(connectionRequest -> new MessageEnvelope<>(IndyMessageTypes.CONNECTION_REQUEST, connectionRequest, null, steward, null)).get().toJSON();

        // #Step 4.1.4 & 4.1.5
        // Steward sends connection request to Faber
        MessageEnvelope<ConnectionRequest> connectionRequestMessageEnvelope = MessageEnvelope.parseFromString(governmentConnectionRequest, newcomer);

        // #Step 4.1.6
        // Faber accepts the connection request from Steward
        ConnectionResponse newcomerConnectionResponse = newcomer.acceptConnectionRequest(connectionRequestMessageEnvelope.getMessage()).get();

        // #Step 4.1.9
        // Faber creates a connection response with its created DID and Nonce from the received request from Steward
        String newcomerConnectionResponseString =  MessageEnvelope.fromAnoncryptable(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, newcomer).toJSON();

        // #Step 4.1.13
        // Steward decrypts the anonymously encrypted message from Faber
        ConnectionResponse connectionResponse = MessageEnvelope.<ConnectionResponse>parseFromString(newcomerConnectionResponseString, steward).getMessage();

        // #Step 4.1.14 & 4.1.15
        // Steward authenticates Faber
        // Steward sends the NYM Transaction for Faber's DID to the ledger
        steward.acceptConnectionResponse(connectionResponse).get();

        // #Step 4.2.1 t/m 4.2.4
        // Faber needs a new DID to interact with identiy owners, thus create a new DID request steward to write on ledger
        String verinymRequest = MessageEnvelope.fromAuthcryptable(newcomer.createVerinymRequest(MessageEnvelope.<ConnectionRequest>parseFromString(governmentConnectionRequest, newcomer).getMessage()
                .getDid()), IndyMessageTypes.VERINYM, newcomer).toJSON();

        // #step 4.2.5 t/m 4.2.8
        // Steward accepts verinym request from Faber and thus writes the new DID on the ledger
        steward.acceptVerinymRequest(MessageEnvelope.<Verinym>parseFromString(verinymRequest, steward).getMessage()).get();
    }

    private static String onboardWalletOwner(TrustAnchor trustAnchor, IndyWallet newcomer) throws IndyException, ExecutionException, InterruptedException, IOException {
        String governmentConnectionRequest = trustAnchor.createConnectionRequest(newcomer.getName(), null)
                .thenApply(connectionRequest -> new MessageEnvelope<>(IndyMessageTypes.CONNECTION_REQUEST, connectionRequest, null, trustAnchor, null)).get().toJSON();


        MessageEnvelope<ConnectionRequest> connectionRequestMessageEnvelope = MessageEnvelope.parseFromString(governmentConnectionRequest, newcomer);
        ConnectionResponse newcomerConnectionResponse = newcomer.acceptConnectionRequest(connectionRequestMessageEnvelope.getMessage()).get();
        String newcomerConnectionResponseString =  MessageEnvelope.fromAnoncryptable(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, newcomer).toJSON();

        ConnectionResponse connectionResponse = MessageEnvelope.<ConnectionResponse>parseFromString(newcomerConnectionResponseString, trustAnchor).getMessage();
        String newcomerDid = trustAnchor.acceptConnectionResponse(connectionResponse).get();

        return newcomerDid;
    }
}
