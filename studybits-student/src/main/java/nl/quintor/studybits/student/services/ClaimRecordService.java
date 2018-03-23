package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.Claim;
import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.repositories.ClaimRecordRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimRecordService {
    private ClaimRecordRepository claimRecordRepository;
    private StudentService studentService;
    private Mapper mapper;

    private ClaimRecord toModel(Object claimRecord) {
        return mapper.map(claimRecord, ClaimRecord.class);
    }

    public ClaimRecord createAndSave(Long studentId, Claim claim) {
        Student student = studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));
        ClaimRecord claimRecord = new ClaimRecord(null, student, claim);

        return claimRecordRepository.save(claimRecord);
    }

    public Optional<ClaimRecord> findById(Long claimId) {
        return claimRecordRepository
                .findById(claimId)
                .map(this::toModel);
    }

    public List<ClaimRecord> findAllClaims(Long studentId) {
        Student owner = studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));

        return claimRecordRepository
                .findAllByOwner(owner)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void updateClaimById(Long studentId, Long claimId, ClaimRecord claimRecord) {
        if (!claimRecordRepository.existsById(claimId))
            throw new IllegalArgumentException("ClaimRecord with id not found.");

        // TODO: Add ownership check

        claimRecord.setId(claimId);
        claimRecordRepository.save(claimRecord);
    }
}
