package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.ConnectionRecord;
import nl.quintor.studybits.student.models.ConnectionRecordModel;
import nl.quintor.studybits.student.services.ConnectionService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/{studentUserName}/connections")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ConnectionRecordController {
    private ConnectionService connectionService;
    private Mapper mapper;

    private ConnectionRecordModel toModel(ConnectionRecord connectionRecord) {
        return mapper.map(connectionRecord, ConnectionRecordModel.class);
    }

    @GetMapping
    List<ConnectionRecordModel> findAllByStudentUserName(@PathVariable String studentUserName) {
        return connectionService
                .findAllByStudentUserName(studentUserName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/{connectionId}")
    ConnectionRecordModel findById(@PathVariable String studentUserName, @PathVariable Long connectionId) {
        // TODO: Check ownership

        return toModel(connectionService.getById(connectionId));
    }

    @PutMapping("/{connectionId}")
    void updateById(@PathVariable String studentUserName, @PathVariable Long connectionId, @RequestBody ConnectionRecordModel connectionRecordModel) {
        ConnectionRecord connectionRecord = mapper.map(connectionRecordModel, ConnectionRecord.class);
        connectionService.updateById(studentUserName, connectionId, connectionRecord);
    }
}

