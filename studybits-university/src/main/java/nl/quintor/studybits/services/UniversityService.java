package nl.quintor.studybits.services;


import nl.quintor.studybits.models.University;
import nl.quintor.studybits.repositories.UniversityRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private Mapper mapper;

    private University toModel(Object university) {
        return mapper.map(university, University.class);
    }

    public List<University> findAll() {
        return universityRepository
                .findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}