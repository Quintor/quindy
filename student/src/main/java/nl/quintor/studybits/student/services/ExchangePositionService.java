package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.entities.ExchangePositionRecord;
import nl.quintor.studybits.student.entities.SchemaDefinitionRecord;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.student.models.ExchangePositionModel;
import nl.quintor.studybits.student.models.ProofRequestInfo;
import nl.quintor.studybits.student.repositories.ExchangePositionRepository;
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

    private final ExchangePositionRepository positionRepository;
    private final StudentService studentService;
    private final UniversityService universityService;
    private final SchemaDefinitionService schemaDefinitionService;
    private final ProofRequestService proofRequestService;
    private final StudentProverService studentProverService;
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
    public List<ExchangePositionRecord> getAllExchangePositionsForStudentName(String studentUserName) {
        return studentService
                .findAllConnectedUniversities(studentUserName)
                .stream()
                .flatMap(this::findAllExchangePositionsForUniversity)
                .collect(Collectors.toList());
    }

    private Stream<ExchangePositionRecord> findAllExchangePositionsForUniversity(University university) {
        return positionRepository
                .findAllByUniversity(university)
                .stream();
    }

    private Stream<ExchangePositionRecord> getExchangePositionFromUniversity(Student student, University university) {
        URI uri = universityService.buildAllExchangePositionsUri(university, student);
        return new RestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<ExchangePositionModel>>() {})
                .getBody()
                .stream()
                .map(this::fromModel);
    }

    private void saveExchangePositionIfNew(ExchangePositionRecord positionRecord) {
        if (!positionRepository.existsByProofRecordIdAndUniversityNameIgnoreCase(positionRecord.getProofRecordId(), positionRecord.getUniversity().getName())) {
            SchemaDefinitionRecord schemaDefinitionRecord = schemaDefinitionService.getOrSave(positionRecord.getSchemaDefinitionRecord());
            positionRecord.setSchemaDefinitionRecord(schemaDefinitionRecord);

            positionRepository.save(positionRecord);
        }
    }

    public void acceptExchangePosition(String studentUserName, ExchangePositionModel positionModel) throws Exception {
        log.info("Student {} - Accepting ExchangePositionModel: {}", studentUserName, positionModel);
        Student student = studentService.getByUserName(studentUserName);
        ExchangePositionRecord record = fromModel(positionModel);

        ProofRequestInfo proofRequestInfo = proofRequestService.getProofRequestForExchangePosition(student, record);
        studentProverService.withProverForStudent(student, prover -> {
            try {
                AuthEncryptedMessageModel messageModel = proofRequestService.getProofForProofRequest(student, prover, proofRequestInfo);
                boolean result = proofRequestService.sendProofToUniversity(messageModel);
                if (!result) {
                    log.error("Could not accept ExchangePosition. Fail upon sending to University.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ExchangePositionRecord fromModel(ExchangePositionModel model) {
        ExchangePositionRecord exchangePosition = mapper.map(model, ExchangePositionRecord.class);

        University university = universityService.getByName(model.getUniversityName());
        SchemaDefinitionRecord schemaDefinition = schemaDefinitionService.getOrSave(model.getSchemaDefinitionRecord());

        exchangePosition.setUniversity(university);
        exchangePosition.setSchemaDefinitionRecord(schemaDefinition);

        return exchangePosition;
    }

}
