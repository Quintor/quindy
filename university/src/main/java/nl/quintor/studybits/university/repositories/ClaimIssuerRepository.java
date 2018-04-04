package nl.quintor.studybits.university.repositories;

import nl.quintor.studybits.university.dto.Claim;
import nl.quintor.studybits.university.entities.ClaimIssuer;
import nl.quintor.studybits.university.entities.ClaimRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimIssuerRepository extends JpaRepository<ClaimIssuer, Long> {
    Optional<ClaimIssuer> findByDid(String did);
}