package nl.quintor.studybits.indy.wrapper;


import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;

import nl.quintor.studybits.indy.wrapper.util.PoolUtils;

import org.hyperledger.indy.sdk.pool.Pool;
import org.junit.Assert;
import org.junit.Test;


import static nl.quintor.studybits.indy.wrapper.TestUtil.removeIndyClientDirectory;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class WalletScenarioIT {

    /*
        Steps refer to the steps in this document: https://github.com/hyperledger/indy-sdk/blob/rc/doc/getting-started/getting-started.md
     */

    @Test
    public void walletScenarioTest() throws Exception {
        // Clear indy_client directory
//        removeIndyClientDirectory();

        // Set pool protocol version based on PoolUtils
        Pool.setProtocolVersion(PoolUtils.PROTOCOL_VERSION).get();

        // Initialize message types for MessageEnvelope
        IndyMessageTypes.init();

        // Create and open Pool with 'default_pool' as pool name
        String poolName = PoolUtils.createPoolLedgerConfig(null, "testPool" + System.currentTimeMillis());
        IndyPool indyPool = new IndyPool(poolName);

        // Generate wallet names
        System.out.println("-------------------------------------");
        System.out.println("Generating base names...");
        System.out.println("-------------------------------------");
        String stewardName = "steward"+System.currentTimeMillis();
        String faberName = "faber"+System.currentTimeMillis();
        String acmeName = "acme"+System.currentTimeMillis();
        String randomName = "random"+System.currentTimeMillis();
        String stewardNameNew = "stewardNew"+System.currentTimeMillis();
        String faberNameNew = "faberNew"+System.currentTimeMillis();
        String acmeNameNew = "acmeNew"+System.currentTimeMillis();
        String randomNameNew = "randomNew"+System.currentTimeMillis();
        System.out.println("GENERATED NAMES:");
        System.out.println("stewardName: " + stewardName);
        System.out.println("faberName: " + faberName);
        System.out.println("acmeName: " + acmeName);
        System.out.println("randomName: " + randomName);
        System.out.println("stewardNameNew: " + stewardNameNew);
        System.out.println("faberNameNew: " + faberNameNew);
        System.out.println("acmeNameNew: " + acmeNameNew);
        System.out.println("randomNameNew: " + randomNameNew);
        System.out.println("-------------------------------------");


        // Generate seeds
        System.out.println("-------------------------------------");
        System.out.println("Generating base seeds...");
        System.out.println("-------------------------------------");
        String stewardSeed = "000000000000000000000000Steward1";
        String faberSeed = IndyWallet.generateSeed();
        String acmeSeed = "0000000000000000000000StaticSeed";
        String randomSeed = IndyWallet.generateSeed();
        System.out.println("GENERATED SEEDS:");
        System.out.println("stewardSeed: " + stewardSeed);
        System.out.println("faberSeed: " + faberSeed);
        System.out.println("acmeSeed: " + acmeSeed);
        System.out.println("randomSeed: " + randomSeed);
        System.out.println("-------------------------------------");

        // Create base wallets
        System.out.println("-------------------------------------");
        System.out.println("Generating base wallets...");
        System.out.println("-------------------------------------");
        IndyWallet stewardWallet = IndyWallet.create(indyPool, stewardName, stewardSeed);
        System.out.println("Generated stewardWallet. [ID: " + stewardName + "] [Seed: " + stewardSeed + "]");
        IndyWallet faberWallet = IndyWallet.create(indyPool, faberName, faberSeed);
        System.out.println("Generated faberWallet. [ID: " + faberName + "] [Seed: " + faberSeed + "]");
        IndyWallet acmeWallet = IndyWallet.create(indyPool, acmeName, acmeSeed);
        System.out.println("Generated acmeWallet. [ID: " + acmeName + "] [Seed: " + acmeSeed + "]");
        IndyWallet radomWallet = IndyWallet.create(indyPool, randomName, randomSeed);
        System.out.println("Generated randomWallet. [ID: " + randomName + "] [Seed: " + randomSeed + "]");
        System.out.println("-------------------------------------");

        // Get DID's from wallets
        System.out.println("-------------------------------------");
        System.out.println("Getting DIDs");
        System.out.println("-------------------------------------");
        System.out.println("DIDs:");
        System.out.println("stewardDid: " + stewardWallet.getMainDid());
        System.out.println("faberDid: " + faberWallet.getMainDid());
        System.out.println("acmeDid: " + acmeWallet.getMainDid());
        System.out.println("randomDid: " + radomWallet.getMainDid());
        System.out.println("-------------------------------------");

        // Create new wallets with same seeds
        System.out.println("-------------------------------------");
        System.out.println("Generating new wallets...");
        System.out.println("-------------------------------------");
        IndyWallet stewardWalletNew = IndyWallet.create(indyPool, stewardNameNew, stewardSeed);
        System.out.println("Generated stewardWallet. [ID: " + stewardNameNew + "] [Seed: " + stewardSeed + "]");
        IndyWallet faberWalletNew = IndyWallet.create(indyPool, faberNameNew, faberSeed);
        System.out.println("Generated faberWallet. [ID: " + faberNameNew + "] [Seed: " + faberSeed + "]");
        IndyWallet acmeWalletNew = IndyWallet.create(indyPool, acmeNameNew, acmeSeed);
        System.out.println("Generated acmeWallet. [ID: " + acmeNameNew + "] [Seed: " + acmeSeed + "]");
        IndyWallet radomWalletNew = IndyWallet.create(indyPool, randomNameNew, randomSeed);
        System.out.println("Generated randomWallet. [ID: " + randomNameNew + "] [Seed: " + randomSeed + "]");
        System.out.println("-------------------------------------");

        // Get DID's from wallets
        System.out.println("-------------------------------------");
        System.out.println("Getting DIDs");
        System.out.println("-------------------------------------");
        System.out.println("DIDs:");
        System.out.println("stewardDid: " + stewardWalletNew.getMainDid());
        System.out.println("faberDid: " + faberWalletNew.getMainDid());
        System.out.println("acmeDid: " + acmeWalletNew.getMainDid());
        System.out.println("randomDid: " + radomWalletNew.getMainDid());
        System.out.println("-------------------------------------");

        assertThat(stewardWallet.getMainDid(), is(equalTo(stewardWalletNew.getMainDid())));
        assertThat(faberWallet.getMainDid(), is(equalTo(faberWalletNew.getMainDid())));
        assertThat(acmeWallet.getMainDid(), is(equalTo(acmeWalletNew.getMainDid())));
        assertThat(radomWallet.getMainDid(), is(equalTo(radomWalletNew.getMainDid())));


    }
}
