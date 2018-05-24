package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.student.entities.ExchangeApplicationRecord;
import nl.quintor.studybits.student.entities.ExchangePositionRecord;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.ExchangeApplicationModel;
import nl.quintor.studybits.student.repositories.ExchangeApplicationRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeApplicationService {

    private final ExchangeApplicationRepository applicationRepository;
    private final StudentService studentService;
    private final UniversityService universityService;
    private final ExchangePositionService exchangePositionService;
    private final Mapper mapper;

    @Transactional
    public void getAndSaveNewExchangeApplications(String studentUserName) {
        Student student = studentService.getByUserName(studentUserName);
        List<University> universities = studentService.findAllConnectedUniversities(studentUserName);
        universities
                .stream()
                .flatMap(university -> getExchangeApplicationFromUniversity(student, university))
                .forEach(this::saveOrUpdateExchangeApplication);
    }

    private Stream<ExchangeApplicationRecord> getExchangeApplicationFromUniversity(Student student, University university) {
        URI uri = universityService.buildAllExchangeApplicationUri(university, student);
        return new RestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<ExchangeApplicationModel>>() {})
                .getBody()
                .stream()
                .map(this::fromModel);
    }

    private void saveOrUpdateExchangeApplication(ExchangeApplicationRecord record) {
        if (applicationRepository.existsByUniversityAndStudentAndExchangePositionRecord(record.getUniversity(), record.getStudent(), record.getExchangePositionRecord()))
            this.updateRecord(record);
        else
            applicationRepository.save(record);
    }

    private void updateRecord(ExchangeApplicationRecord record) {
        Long id = applicationRepository
                .findByUniversityAndStudentAndExchangePositionRecord(record.getUniversity(), record.getStudent(), record.getExchangePositionRecord())
                .orElseThrow(() -> new IllegalArgumentException("Could not find ExchangeApplication in database."))
                .getId();

        record.setId(id);
        applicationRepository.save(record);
    }

    @Transactional
    public List<ExchangeApplicationRecord> getAllByStudentUserName(String studentUserName) {
        return applicationRepository.findAllByStudentUserName(studentUserName);
    }

    private ExchangeApplicationRecord fromModel(ExchangeApplicationModel model) {
        ExchangeApplicationRecord record = mapper.map(model, ExchangeApplicationRecord.class);

        University university = universityService.getByName(model.getUniversityName());
        Student student = studentService.getByUserName(model.getUserName());
        ExchangePositionRecord positionRecord = exchangePositionService.getByProofRecordId(model.getExchangePositionModel().getProofRecordId());

        record.setUniversity(university);
        record.setStudent(student);
        record.setExchangePositionRecord(positionRecord);
        record.getProof().setExchangeApplicationRecord(record);

        return record;
    }
}
