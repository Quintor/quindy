package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.models.StudentClaimInfo;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class ClaimService {

    private final ClaimRecordRepository claimRecordRepository;

    public List<ClaimRecord> findAvailableClaims( Long userId ) {
        return claimRecordRepository.findAllByUserId(userId);
    }
}
