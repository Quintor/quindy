package nl.quintor.studybits.indy.wrapper;

import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import nl.quintor.studybits.indy.wrapper.util.SeedUtil;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.TestUtil.removeIndyClientDirectory;
import static nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class MessageScenarioIT {

    /*
        Steps refer to the steps in this document: https://github.com/hyperledger/indy-sdk/blob/rc/doc/getting-started/getting-started.md
     */

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

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
        TrustAnchor steward = new TrustAnchor(IndyWallet.create(indyPool, "steward", "000000000000000000000000Steward1"));

        steward.acceptVerinymRequest(new Verinym(steward.getMainDid(), steward.getMainDid()));

        // Create new wallet (with DID and VerKey) for Government (TrustAnchor)
        Issuer government = new Issuer(IndyWallet.create(indyPool, "government", SeedUtil.generateSeed()));
        // #Step 4
        // Onboard the issuers (onboard -> verinym -> issuerDids)
        // Onboard the TrustAnchor
        onboardIssuer(steward, government);

        // #Step 4.1.7 & 4.1.8
        // Create new wallet (with DID and VerKey) for Faber (TrustAnchor)
        Issuer faber = new Issuer(IndyWallet.create(indyPool, "faber", SeedUtil.generateSeed()));
        // Onboard the TrustAnchor
        onboardIssuer(steward, faber);

        MessageEnvelopeCodec faberCodec = new MessageEnvelopeCodec(faber);

        // Create new wallet (with DID and VerKey) for ACME (TrustAnchor)
        IndyWallet acmeWallet = IndyWallet.create(indyPool, "acme", SeedUtil.generateSeed());
        Issuer acme = new Issuer(acmeWallet);
        // Onboard the TrustAnchor
        onboardIssuer(steward, acme);

        MessageEnvelopeCodec acmeCodec = new MessageEnvelopeCodec(acme);

        // Create new wallet (with DID and VerKey) for Thrift (TrustAnchor)
        Issuer thriftWallet = new Issuer(IndyWallet.create(indyPool, "thrift", SeedUtil.generateSeed()));
        Issuer thrift = new Issuer(thriftWallet);
        // Onboard the TrustAnchor
        onboardIssuer(steward, thrift);

        MessageEnvelopeCodec thriftCodec = new MessageEnvelopeCodec(thrift);

        // Create new wallet (with DID and VerKey) for Alice (IdentityOwner)
        Prover alice = new Prover(IndyWallet.create(indyPool, "alice", SeedUtil.generateSeed()), "alice_master_secret");

        MessageEnvelopeCodec aliceCodec = new MessageEnvelopeCodec(alice);

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
        String credentialOfferMessageEnvelope = faberCodec.encryptMessage(transcriptCredentialOffer, CREDENTIAL_OFFER, aliceFaberDid).get().toJSON();


        MessageEnvelope<CredentialOffer> convertedEnvelope = MessageEnvelope.parseFromString(credentialOfferMessageEnvelope, CREDENTIAL_OFFER);

        // Alice receives the transcript offer
        CredentialOffer decryptedTranscriptCredentialOffer = aliceCodec.decryptMessage(convertedEnvelope).get();
        // Alice creates a credential request
        CredentialRequest transcriptCredentialRequest = alice.createCredentialRequest(convertedEnvelope.getDid(), decryptedTranscriptCredentialOffer).get();
        String transcriptCredentialRequestMessageEnvelope = aliceCodec.encryptMessage(transcriptCredentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST, convertedEnvelope.getDid()).get().toJSON();


        Map<String, Object> credentialValues  = new HashMap<>();
        credentialValues.put("first_name", "Alice");
        credentialValues.put("last_name", "Garcia");
        credentialValues.put("degree", "Bachelor of Science, Marketing");
        credentialValues.put("status", "graduated");
        credentialValues.put("ssn", "123-45-6789");
        credentialValues.put("year", 2015);
        credentialValues.put("average", 5);

        // Faber receives the credential request from Alice
        CredentialRequest credentialRequest = faberCodec.decryptMessage(MessageEnvelope.parseFromString(transcriptCredentialRequestMessageEnvelope, CREDENTIAL_REQUEST)).get();
        // Faber creates a new credential for Alice and sends it to her
        CredentialWithRequest credential = faber.createCredential(credentialRequest, credentialValues).get();
        String credentialMessageEnvelope = faberCodec.encryptMessage(credential, IndyMessageTypes.CREDENTIAL, aliceFaberDid).get().toJSON();

        // Alice receives her credential and stores it inside her wallet
        CredentialWithRequest decryptedCredential = aliceCodec.decryptMessage(MessageEnvelope.parseFromString(credentialMessageEnvelope, CREDENTIAL)).get();
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

        // ACME Sends proof request to Alice
        String jobApplicationProofRequestString = acmeCodec.encryptMessage(jobApplicationProofRequest, IndyMessageTypes.PROOF_REQUEST, aliceAcmeDid).get().toJSON();
        MessageEnvelope<ProofRequest> jobApplicationProofRequestEnvelope = MessageEnvelope.parseFromString(jobApplicationProofRequestString, PROOF_REQUEST);

        Map<String, String> selfAttestedAttributes = new HashMap<>();
        selfAttestedAttributes.put("first_name", "Alice");
        selfAttestedAttributes.put("last_name", "Garcia");
        selfAttestedAttributes.put("phone_number", "123phonenumber");

        // Alice receives proof request from ACME
        ProofRequest decryptedProofRequest = aliceCodec.decryptMessage(jobApplicationProofRequestEnvelope).get();
        // Alice fufills proof request
        // Create proof object from available credentials for this proof request and self-attested attributes
        Proof proof = alice.fulfillProofRequest(decryptedProofRequest, selfAttestedAttributes).get();
        // Send proof to ACME
        String proofEnvelope = aliceCodec.encryptMessage(proof, IndyMessageTypes.PROOF, jobApplicationProofRequestEnvelope.getDid()).get().toJSON();

        // ACME receives proof
        Proof decryptedProof = acmeCodec.decryptMessage(MessageEnvelope.parseFromString(proofEnvelope, PROOF)).get();

        List<ProofAttribute> attributes = new Verifier(acmeWallet).getVerifiedProofAttributes(jobApplicationProofRequest, decryptedProof, aliceAcmeDid).get();

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
        boolean isValidProof = new Verifier(acmeWallet).validateProof(jobApplicationProofRequest, decryptedProof, aliceAcmeDid).get();
        System.out.println("Degree validation: " + isValidProof);

        Assert.assertTrue(isValidProof);

        // ACME creates a new credential offer (Job certificate) for Alice
        // ACME creates a credential offer for Alice
        CredentialOffer jobCertificateCredentialOffer = acme.createCredentialOffer(jobCertificateCredentialDefId, aliceAcmeDid).get();
        credentialOfferMessageEnvelope = acmeCodec.encryptMessage(jobCertificateCredentialOffer, CREDENTIAL_OFFER, aliceAcmeDid).get().toJSON();

        convertedEnvelope = MessageEnvelope.parseFromString(credentialOfferMessageEnvelope, CREDENTIAL_OFFER);

        // Alice receives the jobCertificate offer
        CredentialOffer decryptedJobCertificateCredentialOffer = aliceCodec.decryptMessage(convertedEnvelope).get();
        // Alice creates a credential request
        CredentialRequest jobCertificateCredentialRequest = alice.createCredentialRequest(convertedEnvelope.getDid(), decryptedJobCertificateCredentialOffer).get();
        String jobCertificateCredentialRequestMessageEnvelope = aliceCodec.encryptMessage(jobCertificateCredentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST, convertedEnvelope.getDid()).get().toJSON();


        Map<String, Object> jobCredentialValues  = new HashMap<>();
        jobCredentialValues.put("first_name", "Alice");
        jobCredentialValues.put("last_name", "Garcia");
        jobCredentialValues.put("salary", 3000);
        jobCredentialValues.put("employee_status", "Permanent");
        jobCredentialValues.put("experience", 5);

        // ACME receives the credential request from Alice
        CredentialRequest jobCredentialRequest = acmeCodec.decryptMessage(MessageEnvelope.parseFromString(jobCertificateCredentialRequestMessageEnvelope, CREDENTIAL_REQUEST)).get();
        // ACME creates a new credential for Alice and sends it to her
        CredentialWithRequest jobCredential = acme.createCredential(jobCredentialRequest, jobCredentialValues).get();
        String jobCredentialMessageEnvelope = acmeCodec.encryptMessage(jobCredential, IndyMessageTypes.CREDENTIAL, aliceAcmeDid).get().toJSON();

        // Alice receives her credential and stores it inside her wallet
        CredentialWithRequest decryptedJobCredential = aliceCodec.decryptMessage(MessageEnvelope.parseFromString(jobCredentialMessageEnvelope, CREDENTIAL)).get();
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

        // Thrift Sends proof request to Alice
        String loanApplicationProofRequestEnvelope = thriftCodec.encryptMessage(loanApplicationProofRequest, IndyMessageTypes.PROOF_REQUEST, aliceThriftDid).get().toJSON();


        Map<String, String> emptyMap = new HashMap<>();

        MessageEnvelope<ProofRequest> convertedLoanApplicationEnvelope = MessageEnvelope.parseFromString(loanApplicationProofRequestEnvelope, PROOF_REQUEST);

        // Alice receives proof request from Thrift
        decryptedProofRequest = aliceCodec.decryptMessage(convertedLoanApplicationEnvelope).get();
        // Alice fufills proof request
        // Create proof object from available credentials for this proof request and self-attested attributes
        Proof jobProof = alice.fulfillProofRequest(decryptedProofRequest, emptyMap).get();
        // Send proof to Thrift
        String jobProofEnvelope = aliceCodec.encryptMessage(jobProof, IndyMessageTypes.PROOF, convertedLoanApplicationEnvelope.getDid()).get().toJSON();

        // Thrift receives proof
        Proof decryptedJobProof = thriftCodec.decryptMessage(MessageEnvelope.parseFromString(jobProofEnvelope, PROOF)).get();

        List<ProofAttribute> jobAttributes = new Verifier(thriftWallet).getVerifiedProofAttributes(loanApplicationProofRequest, decryptedJobProof, aliceThriftDid).get();

        System.out.println(jobAttributes);
        // Thrift Validates proof
        assertThat(jobAttributes, containsInAnyOrder(
                new ProofAttribute("attr1_referent", "employee_status", "Permanent")
        ));

        // Thrift Validates proof
        Boolean jobIsValidProof = new Verifier(thriftWallet).validateProof(loanApplicationProofRequest, decryptedJobProof, aliceThriftDid).get();
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
        }

        // Thrift Sends proof request to Alice
        String loanApplicationPersonalDetailsProofRequestEnvelope = thriftCodec.encryptMessage(loanApplicationPersonalDetailsProofRequest, IndyMessageTypes.PROOF_REQUEST, aliceThriftDid).get().toJSON();

        MessageEnvelope<ProofRequest> convertedLoanApplicationDetailsEnvelope = MessageEnvelope.parseFromString(loanApplicationPersonalDetailsProofRequestEnvelope, PROOF_REQUEST);

        // Alice receives proof request from Thrift
        decryptedProofRequest = aliceCodec.decryptMessage(convertedLoanApplicationDetailsEnvelope).get();

        // Alice fufills proof request
        // Create proof object from available credentials for this proof request and self-attested attributes
        Proof KYCProof = alice.fulfillProofRequest(decryptedProofRequest, emptyMap).get();
        // Send proof to Thrift
        String KYCProofEnvelope = aliceCodec.encryptMessage(KYCProof, IndyMessageTypes.PROOF, convertedLoanApplicationDetailsEnvelope.getDid()).get().toJSON();

        // Thrift receives proof
        Proof decryptedKYCProof = thriftCodec.decryptMessage(MessageEnvelope.parseFromString(KYCProofEnvelope, PROOF)).get();

        List<ProofAttribute> KYCAttributes = new Verifier(thriftWallet).getVerifiedProofAttributes(loanApplicationPersonalDetailsProofRequest, decryptedKYCProof, aliceThriftDid).get();

        System.out.println(KYCAttributes);

        // Thrift Validates proof
        Boolean KYCIsValidProof = new Verifier(thriftWallet).validateProof(loanApplicationProofRequest, decryptedJobProof, aliceThriftDid).get();
        System.out.println("KYC Validation: " + KYCIsValidProof);

        Assert.assertTrue(KYCIsValidProof);
    }

    public static void onboardIssuer(TrustAnchor steward, Issuer newcomer) throws InterruptedException, ExecutionException, IndyException, IOException {
        // Create Codecs to facilitate encryption/decryption
        MessageEnvelopeCodec stewardCodec = new MessageEnvelopeCodec(steward);
        MessageEnvelopeCodec newcomerCodec = new MessageEnvelopeCodec(newcomer);

        // Connecting newcomer with Steward

        // We revert the order from the tutorial, since we use the anoncryption from the verinym

        // Create connection request for steward
        String connectionRequestString = newcomerCodec.encryptMessage(newcomer.createConnectionRequest().get(),
                IndyMessageTypes.CONNECTION_REQUEST, steward.getMainDid()).get().toJSON();

        // Steward decrypts connection request
        ConnectionRequest connectionRequest = stewardCodec.decryptMessage(MessageEnvelope.parseFromString(connectionRequestString, CONNECTION_REQUEST)).get();

        // Steward accepts connection request
        ConnectionResponse newcomerConnectionResponse = steward.acceptConnectionRequest(connectionRequest).get();

        // Steward sends a connection response
        String newcomerConnectionResponseString =  stewardCodec.encryptMessage(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, connectionRequest.getDid()).get().toJSON();


        MessageEnvelope<ConnectionResponse> connectionResponseEnvelope = MessageEnvelope.parseFromString(newcomerConnectionResponseString, CONNECTION_RESPONSE);
        // Newcomer decrypts the connection response
        ConnectionResponse connectionResponse = newcomerCodec.decryptMessage(connectionResponseEnvelope).get();

        // Newcomer accepts connection response
        newcomer.acceptConnectionResponse(connectionResponse, connectionResponseEnvelope.getDid()).get();

        // Faber needs a new DID to interact with identity owners, thus create a new DID request steward to write on ledger
        String verinymRequest = newcomerCodec.encryptMessage(newcomer.createVerinymRequest(connectionResponse.getDid()), IndyMessageTypes.VERINYM, connectionResponse.getDid()).get().toJSON();

        // #step 4.2.5 t/m 4.2.8
        // Steward accepts verinym request from Faber and thus writes the new DID on the ledger
        steward.acceptVerinymRequest(stewardCodec.decryptMessage(MessageEnvelope.parseFromString(verinymRequest, VERINYM)).get()).get();
    }

    private static String onboardWalletOwner(TrustAnchor trustAnchor, IndyWallet newcomer) throws IndyException, ExecutionException, InterruptedException, IOException {
        // Create Codecs to facilitate encryption/decryption
        MessageEnvelopeCodec trustAnchorCodec = new MessageEnvelopeCodec(trustAnchor);
        MessageEnvelopeCodec newcomerCodec = new MessageEnvelopeCodec(newcomer);

        ConnectionRequest newcomerConnectionRequest = newcomer.createConnectionRequest().get();
        // Newcomer creates connection request for trust anchor
        String newcomerConnectionRequestString = newcomerCodec.encryptMessage(newcomerConnectionRequest, IndyMessageTypes.CONNECTION_REQUEST, trustAnchor.getMainDid()).get().toJSON();
        // Newcomer sends connectionRequest to trustAnchor
        MessageEnvelope<ConnectionRequest> connectionRequestMessageEnvelope = MessageEnvelope.parseFromString(newcomerConnectionRequestString, IndyMessageTypes.CONNECTION_REQUEST);

        //TrustAnchor decrypts message from newcomer
        ConnectionRequest connectionRequest = trustAnchorCodec.decryptMessage(connectionRequestMessageEnvelope).get();

        // TrustAnchor accepts connection request
        ConnectionResponse newcomerConnectionResponse = trustAnchor.acceptConnectionRequest(connectionRequest).get();
        // TrustAnchor sends connection response
        String newcomerConnectionResponseString = trustAnchorCodec.encryptMessage(newcomerConnectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, connectionRequest.getDid()).get().toJSON();


        MessageEnvelope<ConnectionResponse> connectionResponseMessageEnvelope = MessageEnvelope.parseFromString(newcomerConnectionResponseString, CONNECTION_RESPONSE);
        // Newcomer accepts connection response
        ConnectionResponse connectionResponse = newcomerCodec.decryptMessage(MessageEnvelope.parseFromString(newcomerConnectionResponseString, CONNECTION_RESPONSE)).get();
        // TrustAnchor accepts the connectionResponse from the newcomer
        newcomer.acceptConnectionResponse(connectionResponse, connectionResponseMessageEnvelope.getDid()).get();


        return newcomerConnectionRequest.getDid();
    }
}
