package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.student.entities.MetaWallet;
import nl.quintor.studybits.student.repositories.MetaWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class MetaWalletService {

    private MetaWalletRepository metaWalletRepository;
    private IndyWalletService indyWalletService;
    private IndyPool indyPool;

    public MetaWallet createAndInit(String userName, String universityName) throws Exception {
        MetaWallet metaWallet = this.create(userName, universityName);

        try (IndyWallet indyWallet = this.openIndyWalletFromMetaWallet(metaWallet)) {
            Prover prover = new Prover(userName, indyPool, indyWallet, userName);
            prover.init();
        }

        return metaWallet;
    }

    private MetaWallet create(String studentUserName, String universityName) throws Exception {

        String walletName = String.format("%s_%s", studentUserName, universityName);

        try (IndyWallet indyWallet = indyWalletService.create(walletName)) {
            MetaWallet metaWallet = new MetaWallet(null, null, indyWallet.getName(), indyWallet.getMainDid(), indyWallet.getMainKey());
            return metaWalletRepository.saveAndFlush(metaWallet);
        }
    }

    public IndyWallet openIndyWalletFromMetaWallet(MetaWallet metaWallet) throws Exception {
        return new IndyWallet(metaWallet.getName(), metaWallet.getMainDid(), metaWallet.getMainKey());
    }

    public void deleteAll() {
        metaWalletRepository
                .findAll()
                .forEach(this::delete);
    }

    @SneakyThrows
    private void delete(MetaWallet metaWallet) {
        IndyWallet indyWallet = openIndyWalletFromMetaWallet(metaWallet);
        indyWallet.close();
        IndyWallet.delete(indyWallet.getName());
        metaWallet.getStudent().setMetaWallet(null);

        metaWalletRepository.delete(metaWallet);
    }
}
