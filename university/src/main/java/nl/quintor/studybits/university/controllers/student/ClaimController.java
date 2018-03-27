package nl.quintor.studybits.university.controllers.student;

import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.helpers.LinkHelper;
import nl.quintor.studybits.university.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.university.models.StudentClaimInfo;
import nl.quintor.studybits.university.services.ClaimProvider;
import nl.quintor.studybits.university.services.ClaimService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/student/{userName}/claims")
public class ClaimController {

    private UserContext userContext;

    private LinkHelper linkHelper;

    private ClaimService claimService;

    private Map<String, ClaimProvider> studentClaimProviderMap;

    @Autowired
    ClaimController(UserContext userContext, LinkHelper linkHelper, ClaimService claimService, ClaimProvider[] claimProviders) {
        this.userContext = userContext;
        this.linkHelper = linkHelper;
        this.claimService = claimService;
        studentClaimProviderMap = Arrays.stream(claimProviders)
                .collect(Collectors.toMap(x -> x.getSchemaName().toLowerCase(), x -> x));
    }

    private ClaimProvider getProvider(String schemaName) {
        Validate.notNull(schemaName, "Schema name cannot be null.");
        return Validate.notNull(studentClaimProviderMap.get(schemaName.toLowerCase()), "Unknown schema.");
    }

    @GetMapping("")
    List<StudentClaimInfo> findAllClaims() {
        return claimService
                .findAvailableClaims(userContext.currentUserId())
                .stream()
                .map(studentClaimInfo -> linkHelper
                        .withLink(studentClaimInfo, ClaimController.class,
                                c -> c.getClaimOffer(studentClaimInfo.getName(), studentClaimInfo.getClaimId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/{schemaName}/{studentClaimId}")
    AuthEncryptedMessageModel getClaimOffer(@PathVariable String schemaName, @PathVariable Long studentClaimId) {
        ClaimProvider provider = getProvider(schemaName);
        AuthEncryptedMessageModel resultModel = provider.getClaimOffer(userContext.currentUserId(), studentClaimId);
        return linkHelper.withLink(resultModel, ClaimController.class, x -> x.getClaim(schemaName, null));
    }

    @PostMapping("/{schemaName}")
    AuthEncryptedMessageModel getClaim(@PathVariable String schemaName, @RequestBody AuthEncryptedMessageModel authEncryptedMessageModel) {
        ClaimProvider provider = getProvider(schemaName);
        return provider.getClaim(userContext.currentUserId(), authEncryptedMessageModel);
    }

}