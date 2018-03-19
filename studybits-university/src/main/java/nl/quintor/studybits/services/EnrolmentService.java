package nl.quintor.studybits.services;

import lombok.SneakyThrows;
import nl.quintor.studybits.dto.ClaimUtils;
import nl.quintor.studybits.dto.Enrolment;
import nl.quintor.studybits.entities.AuthEncryptedMessage;
import nl.quintor.studybits.entities.Student;
import nl.quintor.studybits.entities.StudentClaim;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.Claim;
import nl.quintor.studybits.indy.wrapper.dto.ClaimOffer;
import nl.quintor.studybits.indy.wrapper.dto.ClaimRequest;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import nl.quintor.studybits.models.StudentClaimInfo;
import nl.quintor.studybits.repositories.StudentClaimRepository;
import nl.quintor.studybits.repositories.StudentRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EnrolmentService implements StudentClaimProvider {

    @Autowired
    private IssuerService issuerService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentClaimRepository studentClaimRepository;

    @Autowired
    private Mapper mapper;

    private AuthEncryptedMessage toModel(Object authcryptedMessage) {
        return mapper.map(authcryptedMessage, AuthEncryptedMessage.class);
    }

    private StudentClaimInfo toStudentClaimInfo(Object studentClaim) {
        return mapper.map(studentClaim, StudentClaimInfo.class);
    }

    @SneakyThrows
    private AuthEncryptedMessage toAuthcryptedMessage(Issuer issuer, ClaimOffer claimOffer) {
        return toModel(issuer.authcrypt(claimOffer).get());
    }

    @SneakyThrows
    private AuthEncryptedMessage toAuthcryptedMessage(Issuer issuer, Claim claim) {
        return toModel(issuer.authcrypt(claim).get());
    }

    @Override
    public List<StudentClaimInfo> findAvailableClaims(String universityName, String userName) {
        Stream<StudentClaim> studentClaims = studentRepository
                .findByUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName)
                .map(x -> x.getClaims().stream())
                .orElse(Stream.empty());

        return studentClaims
                .map(this::toStudentClaimInfo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean claimOfferExists(Long studentClaimId) {
        return studentClaimRepository
                .findById(studentClaimId)
                .map(x -> x.getClaimOfferMessage() != null)
                .orElse(false);
    }

    @Override
    public AuthEncryptedMessage createClaimOffer(String universityName, String userName, Long studentClaimId) {
        return withStudentClaim(universityName, userName, studentClaimId, (issuer, studentClaim) -> {
            String academicYear = studentClaim.getClaimLabel();
            Student student = studentClaim.getStudent();
            Validate.validState(student.getAcademicYears().contains(academicYear), "Invalid claim offer request.");
            SchemaKey schemaKey = SchemaKey.fromSchema(ClaimUtils.getSchemaDefinition(Enrolment.class), issuer.getIssuerDid());
            AuthEncryptedMessage authEncryptedMessage = createClaimOffer(issuer, schemaKey, student.getConnection().getDid());
            studentClaim.setClaimOfferMessage(authEncryptedMessage);
            return studentClaimRepository
                    .saveAndFlush(studentClaim)
                    .getClaimOfferMessage();
        });
    }

    @Override
    public AuthEncryptedMessage getClaimOffer(String universityName, String userName, Long studentClaimId) {
        return withStudentClaim(universityName, userName, studentClaimId, (issuer, studentClaim) -> studentClaim.getClaimOfferMessage());
    }

    @Override
    public boolean claimExists(Long studentClaimId) {
        return studentClaimRepository
                .findById(studentClaimId)
                .map(x -> x.getClaimMessage() != null)
                .orElse(false);
    }

    @Override
    public AuthEncryptedMessage createClaim(String universityName, String userName, ClaimRequest claimRequest) {
        return withStudentClaim(universityName, userName, claimRequest, (issuer, studentClaim) -> {
            String academicYear = studentClaim.getClaimLabel();
            Student student = studentClaim.getStudent();
            Validate.validState(student.getAcademicYears().contains(academicYear), "Invalid claim request.");
            Enrolment enrolment = new Enrolment(academicYear);
            AuthEncryptedMessage authEncryptedMessage = createClaim(issuer, claimRequest, enrolment);
            studentClaim.setClaimMessage(authEncryptedMessage);
            return studentClaimRepository
                    .saveAndFlush(studentClaim)
                    .getClaimMessage();
        });
    }

    @Override
    public AuthEncryptedMessage getClaim(String universityName, String userName, ClaimRequest claimRequest) {
        return withStudentClaim(universityName, userName, claimRequest, (issuer, studentClaim) -> studentClaim.getClaimMessage());
    }


    private <R> R withStudentClaim(String universityName, String userName, Long studentClaimId, BiFunction<Issuer, StudentClaim, R> func) {
        StudentClaim studentClaim = studentClaimRepository
                .findById(studentClaimId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student claim id."));
        return withValidStudentClaim(universityName, userName, studentClaim, func);
    }

    private <R> R withStudentClaim(String universityName, String userName, ClaimRequest claimRequest, BiFunction<Issuer, StudentClaim, R> func) {
        StudentClaim example = new StudentClaim();
        example.setClaimName(claimRequest.getSchemaKey().getName());
        example.setClaimVersion(claimRequest.getSchemaKey().getVersion());
        example.setClaimNonce(claimRequest.getNonce());
        StudentClaim studentClaim = studentClaimRepository
                .findOne(Example.of(example))
                .orElseThrow(() -> new IllegalArgumentException("Invalid claim request."));
        return withValidStudentClaim(universityName, userName, studentClaim, func);
    }

    private <R> R withValidStudentClaim(String universityName, String userName, StudentClaim studentClaim, BiFunction<Issuer, StudentClaim, R> func) {
        Issuer issuer = issuerService.getIssuer(universityName);
        Student student = studentClaim.getStudent();
        Validate.isTrue(student.getUserName().equalsIgnoreCase(userName), "Invalid student.");
        Validate.validState(student.getConnection() != null, "Student onboarding incomplete!");
        return func.apply(issuer, studentClaim);
    }

    @SneakyThrows
    private AuthEncryptedMessage createClaimOffer(Issuer issuer, SchemaKey schemaKey, String targetDid) {
        ClaimOffer claimOffer = issuer.createClaimOffer(schemaKey, targetDid).get();
        return toAuthcryptedMessage(issuer, claimOffer);
    }

    @SneakyThrows
    private AuthEncryptedMessage createClaim(Issuer issuer, ClaimRequest claimRequest, Enrolment enrolment) {
        Map<String, Object> claimValues = ClaimUtils.getMapOfClaim(enrolment);
        Claim claim = issuer.createClaim(claimRequest, claimValues).get();
        return toAuthcryptedMessage(issuer, claim);
    }


}
