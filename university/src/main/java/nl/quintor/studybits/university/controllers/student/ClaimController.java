package nl.quintor.studybits.university.controllers.student;

import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.helpers.LinkHelper;
import nl.quintor.studybits.university.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.university.models.StudentClaimInfo;
import nl.quintor.studybits.university.services.ClaimProvider;
import nl.quintor.studybits.university.services.ClaimService;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{universityName}/student/{userName}/claims")
public class ClaimController {

    private final UserContext userContext;
    private final LinkHelper linkHelper;
    private final ClaimService claimService;
    private final Map<String, ClaimProvider> studentClaimProviderMap;
    private final Mapper mapper;

    private AuthEncryptedMessageModel toModel(AuthcryptedMessage authcryptedMessage) {
        return mapper.map(authcryptedMessage, AuthEncryptedMessageModel.class);
    }

    private AuthcryptedMessage toDto(AuthEncryptedMessageModel authEncryptedMessageModel) {
        return mapper.map(authEncryptedMessageModel, AuthcryptedMessage.class);
    }

    @Autowired
    ClaimController(UserContext userContext, LinkHelper linkHelper, ClaimService claimService, ClaimProvider[] claimProviders, Mapper mapper) {
        this.userContext = userContext;
        this.linkHelper = linkHelper;
        this.claimService = claimService;
        studentClaimProviderMap = Arrays.stream(claimProviders)
                .collect(Collectors.toMap(x -> x.getSchemaName().toLowerCase(), x -> x));
        this.mapper = mapper;
    }

    private ClaimProvider getProvider(String schemaName) {
        Validate.notNull(schemaName, "Schema name cannot be null.");
        return Validate.notNull(studentClaimProviderMap.get(schemaName.toLowerCase()), "Unknown schema.");
    }

    @GetMapping
    List<StudentClaimInfo> findAllClaims() {
        return claimService
                .findAvailableClaims(userContext.currentUserId())
                .stream()
                .map(claimRecord -> mapper.map(claimRecord, StudentClaimInfo.class))
                .map(studentClaimInfo -> linkHelper
                        .withLink(studentClaimInfo, ClaimController.class,
                                c -> c.getClaimOffer(studentClaimInfo.getName(), studentClaimInfo.getClaimId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/{schemaName}/{claimRecordId}")
    AuthEncryptedMessageModel getClaimOffer(@PathVariable String schemaName, @PathVariable Long claimRecordId) {
        ClaimProvider provider = getProvider(schemaName);
        AuthcryptedMessage result = provider.getClaimOffer(userContext.currentUserId(), claimRecordId);
        return linkHelper.withLink(toModel(result), ClaimController.class, x -> x.getClaim(schemaName, claimRecordId,null));
    }

    @PostMapping("/{schemaName}/{claimRecordId}")
    AuthEncryptedMessageModel getClaim(@PathVariable String schemaName, @PathVariable Long claimRecordId, @RequestBody AuthEncryptedMessageModel authEncryptedMessageModel) {
        ClaimProvider provider = getProvider(schemaName);
        return toModel(provider.getClaim(userContext.currentUserId(), claimRecordId, toDto(authEncryptedMessageModel)));
    }

}