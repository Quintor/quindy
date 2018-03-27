package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.ConnectionRecord;
import nl.quintor.studybits.student.services.ConnectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor( onConstructor = @__( @Autowired ) )
@RestController
@RequestMapping( "/student/{studentId}/connections" )
public class ConnectionRecordController {
    private final ConnectionRecordService connectionRecordService;

    @GetMapping()
    List<ConnectionRecord> findAllConnections( @PathVariable Long studentId ) {
        return connectionRecordService.findAllConnections(studentId);
    }

    @GetMapping( "/{connectionId}" )
    ConnectionRecord findById( @PathVariable Long studentId, @PathVariable Long connectionId ) {
        return connectionRecordService.findById(connectionId)
                                      .orElseThrow(() -> new IllegalArgumentException("Connection with id not found."));
    }

    @PutMapping( "/{connectionId}" )
    void updateConnectionById( @PathVariable Long studentId, @PathVariable Long connectionId, @RequestBody ConnectionRecord connectionRecord ) {
        connectionRecordService.updateConnectionById(studentId, connectionId, connectionRecord);
    }
}

