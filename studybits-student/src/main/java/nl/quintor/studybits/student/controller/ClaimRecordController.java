package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.Claim;
import nl.quintor.studybits.student.model.ClaimRecord;
import nl.quintor.studybits.student.services.ClaimRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/student/{studentId}")
public class ClaimRecordController {
    private final ClaimRecordService claimRecordService;

    @PostMapping("/claims")
    ClaimRecord createClaimRecord(@PathVariable Long studentId, @RequestParam("claim") Claim claim) {
        return claimRecordService.createAndSave(studentId, claim);
    }

    @GetMapping("/claims")
    List<ClaimRecord> findAllClaims(@PathVariable Long studentId) {
        return claimRecordService.findAllClaims(studentId);
    }

    @GetMapping("/claims/{claimId}")
    ClaimRecord findById(@PathVariable Long studentId, @PathVariable Long claimId) {
        // TODO: Add ownership check.

        return claimRecordService
                .findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim with id not found."));
    }

    @PutMapping("/claims/{claimId}")
    void updateClaimById(@PathVariable Long studentId, @PathVariable Long claimId, @RequestParam("claim") ClaimRecord claimRecord) {
        claimRecordService.updateClaimById(studentId, claimId, claimRecord);
    }
}

