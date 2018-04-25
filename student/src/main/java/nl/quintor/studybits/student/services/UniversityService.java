package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.repositories.UniversityRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UniversityService {
    private UniversityRepository universityRepository;
    private Mapper mapper;

    private University toModel(Object university) {
        return mapper.map(university, University.class);
    }

    public University createAndSave(String name, String endpoint) {
        if (universityRepository.existsByName(name))
            throw new IllegalArgumentException("UniversityModel with name exists already.");

        University university = new University(null, name, endpoint);
        return universityRepository.save(university);
    }

    public Optional<University> findByName(String name) {
        return universityRepository
                .findByName(name);
    }

    public University getByName(String name) {
        return findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("UniversityModel with name not found."));
    }

    public List<University> findAll() {
        return universityRepository
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void updateByObject(University university) {
        Validate.isTrue(!universityRepository.existsById(university.getId()));

        universityRepository.save(university);
    }

    public void deleteByName(String universityName) {
        University university = getByName(universityName);
        universityRepository.deleteById(university.getId());
    }

    private URI buildStudentUri(University university, Student student, String endpoint) {
        return buildStudentUri(university, student.getUserName(), endpoint);
    }

    private URI buildStudentUri(University university, String studentUserName, String endpoint) {
        log.debug("Building onboarding uri on: university endpoint: {}, endpoint: {}, student: {}", university.getEndpoint(), endpoint, studentUserName);
        return UriComponentsBuilder
                .fromHttpUrl(university.getEndpoint())
                .path("/{universityName}/student/{userName}/{endpoint}")
                .build(university.getName(), studentUserName, endpoint);
    }

    public URI buildOnboardingBeginUri(University university, Student student) {
        return buildStudentUri(university, student, "onboarding/begin");
    }

    public URI buildOnboardingFinalizeUri(University university, Student student) {
        return buildStudentUri(university, student, "onboarding/finalize");
    }

    public URI buildCreateStudentUri(University university, Student student) {
        return buildStudentUri(university, student, "students");
    }

    public URI buildAllProofRequestsUri(University university, Student student) {
        return buildStudentUri(university, student, "proofrequests");
    }

    public URI buildGetStudentInfoUri(University university, String userName) {
        return buildStudentUri(university, userName, "students");
    }

    public URI buildAllExchangePositionsUri(University university, Student student) {
        return buildStudentUri(university, student, "positions");
    }

    public URI buildStudentClaimUri(University university, Student student) {
        return buildStudentUri(university, student, "claims");
    }
}
