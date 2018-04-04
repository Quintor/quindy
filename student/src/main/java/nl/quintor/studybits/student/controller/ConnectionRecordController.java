package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.ConnectionRecord;
import nl.quintor.studybits.student.model.ConnectionRecordModel;
import nl.quintor.studybits.student.services.ConnectionRecordService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/student/{studentUserName}/connections")
public class ConnectionRecordController {
    private ConnectionRecordService connectionRecordService;
    private Mapper mapper;

    private ConnectionRecordModel toModel(Object connectionRecord) {
        return mapper.map(connectionRecord, ConnectionRecordModel.class);
    }

    @GetMapping()
    List<ConnectionRecordModel> findAllByStudentUserName(@PathVariable String studentUserName) {
        return connectionRecordService
                .findAllByStudentUserName(studentUserName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/{connectionId}")
    ConnectionRecordModel findById(@PathVariable String studentUserName, @PathVariable Long connectionId) {
        // TODO: Check ownership

        return toModel(connectionRecordService.findByIdOrElseThrow(connectionId));
    }

    @PutMapping("/{connectionId}")
    void updateById(@PathVariable String studentUserName, @PathVariable Long connectionId, @RequestBody ConnectionRecordModel connectionRecordModel) {
        ConnectionRecord connectionRecord = mapper.map(connectionRecordModel, ConnectionRecord.class);
        connectionRecordService.updateById(studentUserName, connectionId, connectionRecord);
    }
}

