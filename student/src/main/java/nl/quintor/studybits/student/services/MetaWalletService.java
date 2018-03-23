package nl.quintor.studybits.student.services;

import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.student.interfaces.MetaWalletRepository;
import nl.quintor.studybits.student.model.MetaWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetaWalletService {
    @Autowired
    private MetaWalletRepository metaWalletRepository;
    @Autowired
    private IndyWalletService indyWalletService;

    public MetaWallet createAndSave(String username) throws Exception {
        IndyWallet indyWallet = indyWalletService.create(username);
        MetaWallet metaWallet = new MetaWallet(null, username, indyWallet.getMainDid(), indyWallet.getMainKey());

        return metaWalletRepository.save(metaWallet);
    }
}
