package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.ProofRequestRecord;
import nl.quintor.studybits.student.models.ProofRequestModel;
import nl.quintor.studybits.student.services.ProofRequestService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/{studentUserName}/proof-requests")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProofRequestContoller {

    private ProofRequestService proofRequestService;
    private Mapper mapper;

    private ProofRequestModel toModel(ProofRequestRecord proofRequestRecord) {
        return mapper.map(proofRequestRecord, ProofRequestModel.class);
    }

    @GetMapping
    List<ProofRequestModel> findAllByStudentUserName(@PathVariable String studentUserName) {
        return proofRequestService
                .findAllByStudentUserName(studentUserName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @PostMapping
    void fulfillProofRequest(@RequestBody ProofRequestModel proofRequestModel) throws Exception {
        ProofRequestRecord proofRequestRecord = proofRequestService.getRecordFromModel(proofRequestModel);
        proofRequestService.fulfillProofRequest(proofRequestRecord);
    }

    @GetMapping("/new")
    void getNewProofRequests(@PathVariable String studentUserName) {
        proofRequestService.getAndSaveNewProofRequests(studentUserName);
    }
}

