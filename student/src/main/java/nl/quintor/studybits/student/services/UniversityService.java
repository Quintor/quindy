package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.repositories.UniversityRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor( onConstructor = @__( @Autowired ) )
@Slf4j
public class UniversityService {
    private UniversityRepository universityRepository;
    private Mapper mapper;

    private University toModel( Object university ) {
        return mapper.map(university, University.class);
    }

    public University createAndSave(String name, String endpoint) {
        if ( universityRepository.existsByName(name) )
            throw new IllegalArgumentException("University with name exists already.");

        University university = new University(null, name, endpoint);
        return universityRepository.save(university);
    }

    public Optional<University> findById( Long uniId ) {
        return universityRepository
                .findById(uniId)
                .map(this::toModel);
    }

    public Optional<University> findByName( String name ) {
        return universityRepository
                .findByName(name)
                .map(this::toModel);
    }

    public List<University> findAll() {
        return universityRepository
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void updateById( University university ) {
        if ( !universityRepository.existsById(university.getId()) )
            throw new IllegalArgumentException("University with id not found.");

        universityRepository.save(university);
    }

    public void deleteById( Long uniId ) {
        if ( !universityRepository.existsById(uniId) )
            throw new IllegalArgumentException("University with id not found.");

        universityRepository.deleteById(uniId);
    }

    public void deleteAll() {
        universityRepository.deleteAll();
    }

    private URI buildOnboardingUri( University university, String endpoint, Student student ) {
        log.debug("Building onboarding uri on: university endpoint: {}, endpoint: {}, student: {}", university.getEndpoint(), endpoint, student);
        return UriComponentsBuilder
                .fromPath(university.getEndpoint())
                .path("/university/{universityName}/onboarding")
                .path("/{endpoint}/{userName}")
                .build(university.getName(), endpoint, student.getUsername());
    }

    public URI buildOnboardingBeginUri( University university, Student student ) {
        return buildOnboardingUri(university, "begin", student);
    }

    public URI buildOnboardingFinalizeUri( University university, Student student ) {
        return buildOnboardingUri(university, "finalize", student);
    }
}
