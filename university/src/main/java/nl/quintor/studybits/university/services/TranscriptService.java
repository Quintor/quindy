package nl.quintor.studybits.university.services;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.dto.ClaimUtils;
import nl.quintor.studybits.university.dto.Transcript;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.TranscriptRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.TranscriptModel;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Slf4j
@Service
public class TranscriptService extends ClaimProvider<Transcript> {

    @Autowired
    public TranscriptService(UniversityService universityService, ClaimRecordRepository claimRecordRepository, UserRepository userRepository, Mapper mapper) {
        super(universityService, claimRecordRepository, userRepository, mapper);
    }

    private TranscriptRecord toEntity(TranscriptModel transcriptModel) {
        return mapper.map(transcriptModel, TranscriptRecord.class);
    }

    @Override
    public String getSchemaName() {
        return ClaimUtils.getSchemaName(Transcript.class);
    }

    @Override
    protected Transcript getClaimForClaimRecord(ClaimRecord claimRecord) {
        String degree = claimRecord.getClaimLabel();
        User user = claimRecord.getUser();
        StudentUser studentUser = user.getStudentUser();
        Validate.validState(studentUser != null, "TranscriptRecord claim is for student users only.");
        TranscriptRecord transcriptRecord = findTranscriptRecord(studentUser, degree)
                .orElseThrow(() -> new IllegalStateException("Invalid claim request. Student user degree not found."));
        return createTranscript(user, transcriptRecord);
    }

    @Transactional
    public void addTranscript(Long userId, TranscriptModel transcriptModel) {
        log.debug("Adding transcript '{}' to userId {}", transcriptModel, userId);
        StudentUser studentUser = userRepository
                .findByStudentUserIsNotNullAndId(userId)
                .map(User::getStudentUser)
                .orElseThrow(() -> new IllegalArgumentException("Student user unknown."));
        if (!findTranscriptRecord(studentUser, transcriptModel.getDegree()).isPresent()) {
            TranscriptRecord transcriptRecord = studentUser.addTranscriptRecord(toEntity(transcriptModel));
            userRepository.saveStudentUser(studentUser);
            addAvailableClaim(userId, createTranscript(studentUser.getUser(), transcriptRecord));
        } else {
            log.debug("TranscriptRecord '{}' already assigned to {}", transcriptModel, studentUser.getUser().getUserName());
        }
    }

    private Optional<TranscriptRecord> findTranscriptRecord(StudentUser studentUser, String degree) {
        return studentUser
                .getTranscriptRecords()
                .stream()
                .filter(x -> x.getDegree().equalsIgnoreCase(degree))
                .findFirst();
    }

    public Transcript createTranscript(User user, TranscriptRecord transcriptRecord) {
        return new Transcript(
                user.getFirstName(),
                user.getLastName(),
                user.getSsn(),
                transcriptRecord.getDegree(),
                transcriptRecord.getStatus(),
                transcriptRecord.getYear(),
                transcriptRecord.getAverage());
    }
}