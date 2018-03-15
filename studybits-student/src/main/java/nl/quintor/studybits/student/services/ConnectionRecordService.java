package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.student.model.ConnectionRecord;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.repositories.ConnectionRecordRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ConnectionRecordService {
    private ConnectionRecordRepository connectionRecordRepository;
    private Mapper mapper;

    private ConnectionRecord toModel(Object connection) {
        return mapper.map(connection, ConnectionRecord.class);
    }

    public void saveConnectionRequest(ConnectionRequest beginRequest, University university, Student student) {
        ConnectionRecord connectionRecord = toModel(beginRequest);
        connectionRecord.setUniversity(university);
        connectionRecord.setStudent(student);

        connectionRecordRepository.save(connectionRecord);
    }
}
