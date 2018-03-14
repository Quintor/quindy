package nl.quintor.studybits.student.interfaces;

import nl.quintor.studybits.student.model.MetaWallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaWalletRepository extends JpaRepository<MetaWallet, Long> {
}
