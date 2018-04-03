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
@RequestMapping("/student/{studentUserName}/claims")
public class ClaimController {
    private final ClaimService claimService;

    @GetMapping
    List<Claim> findAllByOwnerUserName(@PathVariable String studentUserName) {
        return claimService.findAllByOwnerUserName(studentUserName);
    }

    @GetMapping("/schema/{schemaName}")
    List<Claim> findByOwnerUserNameAndSchemaKeyName(@PathVariable String studentUserName, @PathVariable String schemaName) {
        return claimService.findByOwnerUserNameAndSchemaKeyName(studentUserName, schemaName);
    }

    @GetMapping("/{claimId}")
    Claim findById(@PathVariable String studentUserName, @PathVariable Long claimId) {
        // TODO: Add ownership check.

        return claimService.findByIdOrElseThrow(claimId);
    }

    @GetMapping("/new")
    void getAndSaveNewClaimsForOwnerUserName(@PathVariable String studentUserName) throws Exception {
        claimService.getAndSaveNewClaimsForOwnerUserName(studentUserName);
    }
}

