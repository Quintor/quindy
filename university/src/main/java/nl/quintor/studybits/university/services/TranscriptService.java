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
import nl.quintor.studybits.university.repositories.TranscriptRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TranscriptService extends ClaimProvider<Transcript> {

    private TranscriptRecordRepository transcriptRecordRepository;
    private StudentService studentService;

    @Autowired
    public TranscriptService(UniversityService universityService, ClaimRecordRepository claimRecordRepository, UserRepository userRepository, Mapper mapper, TranscriptRecordRepository transcriptRecordRepository, StudentService studentService) {
        super(universityService, claimRecordRepository, userRepository, mapper);
        this.transcriptRecordRepository = transcriptRecordRepository;
    }

    private TranscriptRecord toEntity(TranscriptModel transcriptModel) {
        return mapper.map(transcriptModel, TranscriptRecord.class);
    }

    @Override
    public String getSchemaName() {
        return ClaimUtils.getSchemaName(Transcript.class);
    }

    public List<TranscriptRecord> findAllByUniversity(String universityName) {
        return studentService.findAllForUniversity(universityName)
                .stream()
                .map(User::getStudentUser)
                .flatMap(user -> transcriptRecordRepository.findAllByStudentUser(user).stream())
                .collect(Collectors.toList());
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
    public void addTranscript(String universityName, String studentUserName, TranscriptModel transcriptModel) {
        log.debug("Adding transcript '{}' to userId {}", transcriptModel, studentUserName);
        User user = userRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, studentUserName)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        addTranscript(user, transcriptModel);
    }

    @Transactional
    public void addTranscript(Long studentId, TranscriptModel transcriptModel) {
        addTranscript(userRepository.getOne(studentId), transcriptModel);
    }

    private void addTranscript(User user, TranscriptModel transcriptModel) {
        StudentUser studentUser = Objects.requireNonNull(user.getStudentUser());
        if (!findTranscriptRecord(studentUser, transcriptModel.getDegree()).isPresent()) {
            TranscriptRecord transcriptRecord = studentUser.addTranscriptRecord(toEntity(transcriptModel));
            userRepository.saveStudentUser(studentUser);
            addAvailableClaim(studentUser.getId(), createTranscript(studentUser.getUser(), transcriptRecord));
        } else {
            log.debug("TranscriptRecord '{}' already assigned to {}", transcriptModel, studentUser.getUser()
                    .getUserName());
        }
    }

    private Optional<TranscriptRecord> findTranscriptRecord(StudentUser studentUser, String degree) {
        return studentUser
                .getTranscriptRecords()
                .stream()
                .filter(x -> x.getDegree().equalsIgnoreCase(degree))
                .findFirst();
    }

    private Transcript createTranscript(User user, TranscriptRecord transcriptRecord) {
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