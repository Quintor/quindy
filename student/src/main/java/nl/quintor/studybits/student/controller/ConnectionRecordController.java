package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.ConnectionRecord;
import nl.quintor.studybits.student.services.ConnectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/student/{studentUserName}/connections")
public class ConnectionRecordController {
    private final ConnectionRecordService connectionRecordService;

    @GetMapping()
    List<ConnectionRecord> findAllByStudentUserName(@PathVariable String studentUserName) {
        return connectionRecordService.findAllByStudentUserName(studentUserName);
    }

    @GetMapping("/{connectionId}")
    ConnectionRecord findById(@PathVariable String studentUserName, @PathVariable Long connectionId) {
        // TODO: Check ownership

        return connectionRecordService.findByIdOrElseThrow(connectionId);
    }

    @PutMapping("/{connectionId}")
    void updateById(@PathVariable String studentUserName, @PathVariable Long connectionId, @RequestBody ConnectionRecord connectionRecord) {
        connectionRecordService.updateById(studentUserName, connectionId, connectionRecord);
    }
}

