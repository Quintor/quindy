package nl.quintor.studybits.student.controller;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.Claim;
import nl.quintor.studybits.student.entities.University;
import nl.quintor.studybits.student.models.ClaimModel;
import nl.quintor.studybits.student.services.ClaimService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/{studentUserName}/claims")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClaimController {
    private ClaimService claimService;
    private Mapper mapper;

    private ClaimModel toModel(Claim claim) {
        ClaimModel claimModel = mapper.map(claim, ClaimModel.class);
        University university = claim.getOwner().getOriginUniversity();
        if(university != null) {
            claimModel.setUniversityName(university.getName());
        }
        return claimModel;
    }

    @GetMapping
    List<ClaimModel> findAllByOwnerUserName(@PathVariable String studentUserName) {
        return claimService
                .findAllByOwnerUserName(studentUserName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/schema/{schemaName}")
    List<ClaimModel> findByOwnerUserNameAndSchemaKeyName(@PathVariable String studentUserName, @PathVariable String schemaName) {
        return claimService
                .findByOwnerUserNameAndSchemaKeyName(studentUserName, schemaName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/{claimId}")
    ClaimModel findById(@PathVariable String studentUserName, @PathVariable Long claimId) {
        // TODO: Add ownership check.

        return toModel(claimService.findByIdOrElseThrow(claimId));
    }

    @GetMapping("/new")
    void getAndSaveNewClaimsForOwnerUserName(@PathVariable String studentUserName) throws Exception {
        claimService.getAndSaveNewClaimsForOwnerUserName(studentUserName);
    }
}

