package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.entities.ClaimIssuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimIssuerRepository extends JpaRepository<ClaimIssuer, Long> {
    Optional<ClaimIssuer> findByDid(String did);
}