package nl.quintor.studybits.university.controllers.student;

import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.models.StudentClaimInfo;
import nl.quintor.studybits.university.services.ClaimProvider;
import nl.quintor.studybits.university.services.ClaimService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.hateoas.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping( "/{universityName}/student/{userName}/claims" )
public class ClaimController {

    private UserContext userContext;

    private ClaimService claimService;

    private Map<String, ClaimProvider> studentClaimProviderMap;

    @Autowired
    ClaimController( UserContext userContext, ClaimService claimService, ClaimProvider[] claimProviders ) {
        this.userContext = userContext;
        this.claimService = claimService;
        studentClaimProviderMap = Arrays.stream(claimProviders)
                                        .collect(Collectors.toMap(x -> x.getSchemaName(), x -> x));
    }

    private ClaimProvider getProvider( String schemaName ) {
        return Validate.notNull(studentClaimProviderMap.get(schemaName), "Unknown claim provider.");
    }

    @GetMapping( "" )
    List<StudentClaimInfo> findAllClaims() {
        return studentClaimProviderMap.entrySet()
                                      .stream()
                                      .flatMap(x -> getProviderClaims(x.getKey(), x.getValue()))
                                      .collect(Collectors.toList());
    }

    @GetMapping( "/{schemaName}/{studentClaimId}" )
    AuthcryptedMessage getClaimOffer( @PathVariable String schemaName, @PathVariable Long studentClaimId ) {
        ClaimProvider provider = getProvider(schemaName);
        return provider.getClaimOffer(userContext.currentUserId(), studentClaimId);
    }

    @PostMapping( "/{schemaName}" )
    AuthcryptedMessage getClaim( @RequestBody String schemaName, @RequestBody AuthcryptedMessage authcryptedMessage ) {
        ClaimProvider provider = getProvider(schemaName);
        return provider.getClaim(userContext.currentUserId(), authcryptedMessage);
    }

    private Stream<StudentClaimInfo> getProviderClaims( String provider, ClaimProvider claimProvider ) {
        return claimService.findAvailableClaims(userContext.currentUserId())
                           .stream()
                           .map(x -> withClaimOfferLink(provider, x));
    }

    @SneakyThrows
    private StudentClaimInfo withClaimOfferLink( String provider, StudentClaimInfo studentClaimInfo ) {
        AuthcryptedMessage proxyClaimController = methodOn(ClaimController.class).getClaimOffer(provider, studentClaimInfo.getClaimId());
        Link link = linkTo(proxyClaimController).withRel("ClaimOffer")
                                                .expand(userContext.getIdentityPathVariables());
        studentClaimInfo.add(link);
        return studentClaimInfo;
    }

}