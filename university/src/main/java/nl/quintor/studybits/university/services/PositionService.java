package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.PositionRecord;
import nl.quintor.studybits.university.models.PositionModel;
import nl.quintor.studybits.university.repositories.PositionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PositionService {

    private final PositionRecordRepository positionRecordRepository;

    public void create(PositionModel position) {
        // TODO: Implement create new position
    }

    public List<PositionRecord> findAll() {
        return positionRecordRepository.findAll();
    }
}
