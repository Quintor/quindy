package nl.quintor.studybits.student.services;

import nl.quintor.studybits.student.interfaces.UniversityRepository;
import nl.quintor.studybits.student.model.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniversityService {
    @Autowired
    private UniversityRepository universityRepository;

    public Boolean exists(String name) {
        return getByName(name) != null;
    }

    public University getByName(String name) {
        return universityRepository.getByName(name);
    }

    public University createAndSave(String name, String endpoint) {
        University university = new University(null, name, endpoint);

        return universityRepository.save(university);
    }
}
