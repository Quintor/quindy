package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.student.interfaces.ClaimOfferRecordRepository;
import nl.quintor.studybits.student.model.Claim;
import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.model.Student;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimRecordService {
    private ClaimOfferRecordRepository claimOfferRecordRepository;
    private StudentService studentService;
    private Mapper mapper;

    private ClaimRecord toModel(Object claimRecord) {
        return mapper.map(claimRecord, ClaimRecord.class);
    }

    public List<ClaimRecord> findAllClaims(Long studentId) {
        studentService.checkIfPresentOrElseThrow(studentId);

        return claimOfferRecordRepository
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public Optional<ClaimRecord> findById(Long claimId) {
        return claimOfferRecordRepository.findById(claimId);
    }

    @SneakyThrows
    public ClaimRecord createAndSave(Long studentId, Claim claim) {
        Student student = studentService
                .findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with id not found."));
        ClaimRecord claimRecord = new ClaimRecord(student, claim);

        return claimOfferRecordRepository.save(claimRecord);
    }

    @SneakyThrows
    public ClaimRecord updateClaimById(Long studentId, Long claimId, ClaimRecord claimRecord) {
        claimRecord.setId(claimId);
        return claimOfferRecordRepository.save(claimRecord);
    }
}
