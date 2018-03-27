package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.student.model.ConnectionRecord;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import nl.quintor.studybits.student.repositories.ConnectionRecordRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor( onConstructor = @__( @Autowired ) )
public class ConnectionRecordService {
    private ConnectionRecordRepository connectionRecordRepository;
    private Mapper mapper;

    private ConnectionRecord toModel( Object connection ) {
        return mapper.map(connection, ConnectionRecord.class);
    }

    public void saveConnectionRequest( ConnectionRequest beginRequest, University university, Student student ) {
        ConnectionRecord connectionRecord = toModel(beginRequest);
        connectionRecord.setUniversity(university);
        connectionRecord.setStudent(student);

        connectionRecordRepository.save(connectionRecord);
    }

    public Optional<ConnectionRecord> findById( Long connectionId ) {
        return connectionRecordRepository.findById(connectionId)
                                         .map(this::toModel);
    }

    public List<ConnectionRecord> findAllConnections( Long studentId ) {
        return connectionRecordRepository.findAllByStudent_Id(studentId)
                                         .stream()
                                         .map(this::toModel)
                                         .collect(Collectors.toList());
    }

    public void updateConnectionById( Long studentId, Long connectionId, ConnectionRecord connectionRecord ) {
        Validate.isTrue(connectionRecordRepository.existsById(connectionId));

        connectionRecord.setId(connectionId);
        connectionRecordRepository.save(connectionRecord);
    }

    public void deleteAll() {
        connectionRecordRepository.deleteAll();
    }
}
