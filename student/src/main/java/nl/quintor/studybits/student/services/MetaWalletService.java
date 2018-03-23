package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.student.model.MetaWallet;
import nl.quintor.studybits.student.repositories.MetaWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor( onConstructor = @__( @Autowired ) )
public class MetaWalletService {
    private MetaWalletRepository metaWalletRepository;
    private IndyWalletService indyWalletService;

    public MetaWallet createAndSave(String username) throws Exception {
        IndyWallet indyWallet = indyWalletService.create(username);
        MetaWallet metaWallet = new MetaWallet(null, username, indyWallet.getMainDid(), indyWallet.getMainKey());

        return metaWalletRepository.save(metaWallet);
    }

    @SneakyThrows
    public IndyWallet createIndyWalletFromMetaWallet( MetaWallet metaWallet ) {
        return new IndyWallet(metaWallet.getName(), metaWallet.getMainDid(), metaWallet.getMainKey());
    }
}
