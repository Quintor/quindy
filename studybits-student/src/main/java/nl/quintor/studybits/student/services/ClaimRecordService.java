package nl.quintor.studybits.student.services;

import javassist.NotFoundException;
import nl.quintor.studybits.student.interfaces.ClaimOfferRecordRepository;
import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClaimRecordService {
    @Autowired
    private ClaimOfferRecordRepository claimOfferRecordRepository;

    @Autowired
    private StudentService studentService;

    public List<ClaimRecord> getAllClaimsForStudent(Long studentId) throws Exception {
        Student student = studentService.getById(studentId);
        if (student == null)
            throw new NotFoundException("Student with username not found.");

        return claimOfferRecordRepository.findAllByOwner(student);
    }

    public ClaimRecord getClaimForStudent(Long claimId, Long studentId) throws Exception {
        ClaimRecord claimRecord = claimOfferRecordRepository.getById(claimId);
        ensureOwnership(claimRecord, studentId);

        return claimRecord;
    }

    public void ensureOwnership(ClaimRecord claimRecord, long studentId) throws Exception {
        if (claimRecord.getOwner().getId().equals(studentId))
            throw new NotFoundException("Claim with id not found for student.");
    }
}
