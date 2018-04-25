package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.student.entities.ConnectionRecord;
import nl.quintor.studybits.student.entities.Student;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.repositories.ConnectionRecordRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ConnectionService {
    private ConnectionRecordRepository connectionRecordRepository;
    private Mapper mapper;

    private ConnectionRecord toModel(Object connection) {
        return mapper.map(connection, ConnectionRecord.class);
    }

    public void save(ConnectionRequest beginRequest, University university, Student student) {
        ConnectionRecord connectionRecord = toModel(beginRequest);
        connectionRecord.setUniversity(university);
        connectionRecord.setStudent(student);
        connectionRecord.setConfirmed(university.equals(student.getOriginUniversity()));

        connectionRecordRepository.save(connectionRecord);
    }

    public ConnectionRecord getById(Long connectionId) {
        return connectionRecordRepository
                .findById(connectionId)
                .map(this::toModel)
                .orElseThrow(() -> new IllegalArgumentException("ConnectionRecord with id not found."));
    }

    public ConnectionRecord getByStudentAndUniversity(Student student, University university) {
        return connectionRecordRepository
                .findByStudentAndUniversity(student, university)
                .orElseThrow(() -> new IllegalArgumentException("Could not find connection record for student and university."));
    }

    public List<ConnectionRecord> findAllByStudentUserName(String studentUserName) {
        return connectionRecordRepository
                .findAllByStudentUserName(studentUserName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void updateById(String studentUserName, Long connectionId, ConnectionRecord connectionRecord) {
        Validate.isTrue(connectionRecordRepository.existsById(connectionId));
        connectionRecordRepository.save(connectionRecord);
    }

    public void setConfirmed(Student student, University university, Boolean isConfirmed) {
        ConnectionRecord connectionRecord = getByStudentAndUniversity(student, university);
        connectionRecord.setConfirmed(isConfirmed);

        connectionRecordRepository.save(connectionRecord);
    }
}
