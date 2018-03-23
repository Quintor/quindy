package nl.quintor.studybits.student.repositories;

import nl.quintor.studybits.student.model.MetaWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaWalletRepository extends JpaRepository<MetaWallet, Long> {
}
