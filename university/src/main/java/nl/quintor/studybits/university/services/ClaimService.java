package nl.quintor.studybits.university.services;

import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.models.StudentClaimInfo;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    @Autowired
    protected Mapper mapper;
    @Autowired
    private ClaimRecordRepository claimRecordRepository;

    private StudentClaimInfo toStudentClaimInfo( Object claimRecord ) {
        return mapper.map(claimRecord, StudentClaimInfo.class);
    }

    public List<StudentClaimInfo> findAvailableClaims( Long userId ) {
        List<ClaimRecord> claimRecords = claimRecordRepository.findAllByUserId(userId);

        return claimRecords.stream()
                           .map(this::toStudentClaimInfo)
                           .collect(Collectors.toList());
    }
}
