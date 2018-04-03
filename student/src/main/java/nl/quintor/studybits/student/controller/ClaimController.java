package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.model.Claim;
import nl.quintor.studybits.student.services.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/student/{studentId}/claims")
public class ClaimController {
    private final ClaimService claimService;

    @GetMapping
    List<Claim> findAllClaims(@PathVariable Long studentId) {
        return claimService.findAllClaims(studentId);
    }

    @GetMapping("/schema/{schemaName}")
    List<Claim> findClaimsByIdAndSchemaName(@PathVariable Long studentId, @PathVariable String schemaName) {
        return claimService.findClaimsByIdAndSchemaName(studentId, schemaName);
    }

    @GetMapping("/{claimId}")
    Claim findById(@PathVariable Long studentId, @PathVariable Long claimId) {
        // TODO: Add ownership check.

        return claimService.findById(claimId);
    }

    @GetMapping("/new")
    void getNewClaims(@PathVariable Long studentId) throws Exception {
        claimService.getAndSaveNewClaimsForStudentId(studentId);
    }
}

