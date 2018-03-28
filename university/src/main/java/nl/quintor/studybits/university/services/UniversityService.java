package nl.quintor.studybits.university.services;


import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.dto.Schema;
import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import nl.quintor.studybits.university.dto.Claim;
import nl.quintor.studybits.university.entities.ClaimSchema;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.models.UniversityModel;
import nl.quintor.studybits.university.repositories.ClaimSchemaRepository;
import nl.quintor.studybits.university.repositories.UniversityRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class UniversityService {

    private static final boolean LAZY_ISSUER_CREATION = true;

    private final UniversityRepository universityRepository;
    private final ClaimSchemaRepository claimSchemaRepository;
    private final IssuerService issuerService;
    private final Mapper mapper;

    private UniversityModel toModel(University university ) {
        return mapper.map(university, UniversityModel.class);
    }

    private University toEntity(UniversityModel universityModel ) {
        return mapper.map(universityModel, University.class);
    }

    public List<UniversityModel> findAll() {
        return universityRepository.findAll()
                                   .stream()
                                   .map(this::toModel)
                                   .collect(Collectors.toList());
    }


    public UniversityModel create(String universityName) {
        University university = universityRepository.save(new University(null, universityName, new ArrayList<>()));
        if(LAZY_ISSUER_CREATION) {
            issuerService.ensureIssuer(universityName);
        }
        return toModel(university);
    }



    @SneakyThrows
    public SchemaKey defineSchema(String universityName, SchemaDefinition schemaDefinition) {
        Issuer issuer = getIssuer(universityName);
        return issuer.createAndSendSchema(schemaDefinition).get();
    }

    @SneakyThrows
    @Transactional
    public void addClaimSchema(String universityName, SchemaKey schemaKey, boolean canIssueClaim) {
        University university = getUniversity(universityName);
        ClaimSchema claimSchema = new ClaimSchema(null, university, schemaKey.getName(), schemaKey.getVersion(), schemaKey.getDid(), canIssueClaim);
        university.getClaimSchemas().add(claimSchema);
        universityRepository.save(university);
        if(canIssueClaim) {
            Issuer issuer = getIssuer(universityName);
            issuer.defineClaim(schemaKey).get();
        }
    }

    public SchemaKey getSchemaKey(String universityName, SchemaDefinition schemaDefinition) {
        ClaimSchema claimSchema = getClaimSchema(universityName, schemaDefinition);
        return new SchemaKey(claimSchema.getSchemaName(), claimSchema.getSchemaVersion(), claimSchema.getSchemaIssuerDid());
    }


    private ClaimSchema getClaimSchema(String universityName, SchemaDefinition schemaDefinition) {
        return claimSchemaRepository
                .findByUniversityNameIgnoreCaseAndSchemaNameAndSchemaVersion(
                        universityName,
                        schemaDefinition.getName(),
                        schemaDefinition.getVersion()
                ).orElseThrow(() -> new IllegalArgumentException("Schema key not found."));
    }

    public Issuer getIssuer(String universityName) {
        return issuerService.ensureIssuer(universityName);
    }

    private University getUniversity(String universityName) {
        return universityRepository
                .findByNameIgnoreCase(universityName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown university."));
    }

}