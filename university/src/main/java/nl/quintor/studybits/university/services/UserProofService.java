package nl.quintor.studybits.university.services;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.dto.UserProof;
import nl.quintor.studybits.university.entities.ProofRecord;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserProofService extends ProofHandler<UserProof>  {

    @Autowired
    public UserProofService(UniversityService universityService, ProofRecordRepository proofRecordRepository, ClaimSchemaRepository claimSchemaRepository, UserRepository userRepository, Mapper mapper) {
        super(universityService, proofRecordRepository, claimSchemaRepository, userRepository, mapper);
    }

    @Override
    protected Class<UserProof> getProofType() {
        return UserProof.class;
    }

    @Override
    protected boolean handleProof(User user, ProofRecord proofRecord, UserProof proof) {
        Validate.isTrue(user.getFirstName().equals(proof.getFirstName()), "Firstname mismatch." );
        Validate.isTrue(user.getLastName().equals(proof.getLastName()), "Lastname mismatch.");
        Validate.isTrue(user.getSsn().equals(proof.getSsn()), "Ssn mismatch.");
        user.setConfirmed(true);
        userRepository.save(user);
        return true;
    }

}