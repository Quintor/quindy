package nl.quintor.studybits.university.services;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.university.dto.UserProof;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import nl.quintor.studybits.university.repositories.ProofRecordRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
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
}