package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.entities.ExchangePositionRecord;
import nl.quintor.studybits.student.entities.SchemaDefinitionRecord;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.ExchangePositionModel;
import nl.quintor.studybits.student.repositories.ExchangePositionRecordRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangePositionService {

    private final ExchangePositionRecordRepository positionRepository;
    private final StudentService studentService;
    private final UniversityService universityService;
    private final SchemaDefinitionService schemaDefinitionService;
    private final Mapper mapper;

    @Transactional
    public void getAndSaveNewExchangePositions(String studentUserName) {
        Student student = studentService.getByUserName(studentUserName);
        List<University> universities = studentService.findAllConnectedUniversities(studentUserName);
        universities
                .stream()
                .flatMap(university -> getExchangePositionFromUniversity(student, university))
                .forEach(this::saveExchangePositionIfNew);
    }

    @Transactional
    public List<ExchangePositionRecord> getAllForStudentName(String studentUserName) {
        return studentService
                .findAllConnectedUniversities(studentUserName)
                .stream()
                .flatMap(this::getAllForUniversity)
                .collect(Collectors.toList());
    }

    private Stream<ExchangePositionRecord> getAllForUniversity(University university) {
        return positionRepository
                .findAllByUniversity(university)
                .stream();
    }

    private Stream<ExchangePositionRecord> getExchangePositionFromUniversity(Student student, University university) {
        URI uri = universityService.buildAllExchangePositionsUri(university, student);
        return new RestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<ExchangePositionModel>>() {})
                .getBody()
                .stream()
                .map(model -> this.fromModel(model, student));
    }

    private void saveExchangePositionIfNew(ExchangePositionRecord positionRecord) {
        if (!positionRepository.existsByUniversitySeqNoAndUniversityName(positionRecord.getUniversitySeqNo(), positionRecord.getUniversity().getName())) {
            SchemaDefinitionRecord schemaDefinitionRecord = schemaDefinitionService.getOrSave(positionRecord.getSchemaDefinitionRecord());
            positionRecord.setSchemaDefinitionRecord(schemaDefinitionRecord);

            positionRepository.save(positionRecord);
        }
    }

    private ExchangePositionRecord fromModel(ExchangePositionModel model, Student student) {
        ExchangePositionRecord record = mapper.map(model, ExchangePositionRecord.class);

        University university = universityService.getByName(model.getUniversityName());
        record.setUniversity(university);

        return record;
    }

}
