package nl.quintor.studybits.university.services;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.dto.ClaimUtils;
import nl.quintor.studybits.university.dto.Enrolment;
import nl.quintor.studybits.university.entities.ClaimRecord;
import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.ClaimRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class EnrolmentService extends ClaimProvider<Enrolment> {

    @Autowired
    public EnrolmentService(UniversityService universityService, ClaimRecordRepository claimRecordRepository, UserRepository userRepository, Mapper mapper) {
        super(universityService, claimRecordRepository, userRepository, mapper);
    }

    @Override
    public String getSchemaName() {
        return ClaimUtils.getVersion(Enrolment.class).getName();
    }

    @Override
    protected Enrolment getClaimForClaimRecord(ClaimRecord claimRecord) {
        String academicYear = claimRecord.getClaimLabel();
        User user = claimRecord.getUser();
        StudentUser studentUser = Objects.requireNonNull(user.getStudentUser(), "Enrolment claim is for student users only.");
        Set<String> academicYears = Objects.requireNonNull(studentUser.getAcademicYears());
        Validate.validState(academicYears.contains(academicYear), "Invalid claim request.");
        return createEnrolment(user, academicYear);
    }

    public void addEnrolment(Long userId, String academicYear) {
        log.debug("Adding academic year '{}' to userId {}", academicYear, userId);
        StudentUser studentUser = userRepository
                .findByStudentUserIsNotNullAndId(userId)
                .map(User::getStudentUser)
                .orElseThrow(() -> new IllegalArgumentException("Student user unknown."));
        if (studentUser.getAcademicYears().add(academicYear)) {
            userRepository.saveStudentUser(studentUser);
            addAvailableClaim(userId, createEnrolment(studentUser.getUser(), academicYear));
        } else {
            log.debug("Academic year '{}' already assigned to {}", academicYear, studentUser.getUser().getUserName());
        }
    }

    private Enrolment createEnrolment(User user, String academicYear) {
        return new Enrolment(user.getFirstName(), user.getLastName(), user.getSsn(), academicYear);
    }
}
