package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.repositories.UniversityRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UniversityService {
    private UniversityRepository universityRepository;
    private Mapper mapper;

    private University toModel(Object university) {
        return mapper.map(university, University.class);
    }

    public boolean existsByName(String name) {
        return universityRepository.existsByName(name);
    }

    public University createAndSave(String name, String endpoint) {
        if (universityRepository.existsByName(name))
            throw new IllegalArgumentException("University with name exists already.");

        University university = new University(null, name, endpoint);
        return universityRepository.save(university);
    }

    public Optional<University> findById(Long uniId) {
        return universityRepository.findById(uniId);
    }

    public Optional<University> findByName(String name) {
        return universityRepository.findByName(name);
    }

    public List<University> findAll() {
        return universityRepository
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void updateById(Long uniId, University university) {
        if (!universityRepository.existsById(uniId))
            throw new IllegalArgumentException("University with id not found.");

        university.setId(uniId);
        universityRepository.save(university);
    }

    public void deleteById(Long uniId) {
        if (!universityRepository.existsById(uniId))
            throw new IllegalArgumentException("University with id not found.");

        universityRepository.deleteById(uniId);
    }
}
