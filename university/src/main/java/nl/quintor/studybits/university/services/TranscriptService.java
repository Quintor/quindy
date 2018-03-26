package nl.quintor.studybits.university.services;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.dto.ClaimUtils;
import nl.quintor.studybits.university.dto.Transcript;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.TranscriptRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.TranscriptModel;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@Service
public class TranscriptService extends ClaimProvider<Transcript> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Mapper mapper;

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
        TranscriptRecord transcriptRecord = studentUser
                .getTranscriptRecords()
                .stream()
                .filter(x -> x.getDegree().equalsIgnoreCase(degree))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid claim request. Student user degree not found."));
        return createTranscript(user, transcriptRecord);
    }

    @Transactional
    public void addTranscript(Long userId, TranscriptModel transcriptModel) {
        log.debug("Adding transcript '{}' to userId {}", transcriptModel, userId);
        StudentUser studentUser = userRepository
                .findAllByStudentUserIsNotNullAndId(userId)
                .map(User::getStudentUser)
                .orElseThrow(() -> new IllegalArgumentException("Student user unknown."));
        if (studentUser.getTranscriptRecords().stream().noneMatch(x ->
                x.getDegree().equalsIgnoreCase(transcriptModel.getDegree()))) {
            TranscriptRecord transcriptRecord = toEntity(transcriptModel);
            transcriptRecord.setStudentUser(studentUser);
            studentUser.getTranscriptRecords().add(transcriptRecord);
            userRepository.saveStudentUser(studentUser);
            addAvailableClaim(userId, createTranscript(studentUser.getUser(), transcriptRecord));
        } else {
            log.debug("TranscriptRecord '{}' already assigned to {}", transcriptModel, studentUser.getUser().getUserName());
        }
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