package nl.quintor.studybits.university.controllers;


import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.university.models.StudentClaimInfo;
import nl.quintor.studybits.university.services.StudentClaimProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.hateoas.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/{universityName}/claims")
public class ClaimController {


    private Map<String, StudentClaimProvider> studentClaimProviderMap;

    private Optional<StudentClaimProvider> getProvider(String provider) {
        return Optional.ofNullable(studentClaimProviderMap.get(provider));
    }

    @Autowired
    ClaimController(StudentClaimProvider[] studentClaimProviders) {
        studentClaimProviderMap = Arrays.stream(studentClaimProviders)
                .collect(Collectors.toMap(x -> x.getClaimProviderId().toLowerCase(),  x -> x));
    }



    @GetMapping("/{userName}")
    List<StudentClaimInfo> all(@PathVariable String universityName, @PathVariable String userName) {
        return studentClaimProviderMap
                .values()
                .stream()
                .flatMap(x -> getProviderClaims(universityName, userName, x))
                .collect(Collectors.toList());
    }

    private Stream<StudentClaimInfo> getProviderClaims(String universityName, String userName, StudentClaimProvider studentClaimProvider) {
        String provider = studentClaimProvider.getClaimProviderId();
        return studentClaimProvider
                .findAvailableClaims(universityName, userName)
                .stream()
                .map(x -> withClaimOfferLink(universityName, userName, provider, x));
    }


    @SneakyThrows
    private StudentClaimInfo withClaimOfferLink(String universityName, String userName, String provider, StudentClaimInfo studentClaimInfo) {
        AuthcryptedMessage methodLinkBuilder = methodOn(ClaimController.class)
                .getClaimOffer(universityName, userName, provider, studentClaimInfo.getClaimId());
        Link link = linkTo(methodLinkBuilder).withRel("ClaimOffer");
        studentClaimInfo.add(link);
        return studentClaimInfo;
    }

    @GetMapping("/{userName}/{provider}/{studentClaimId}")
    AuthcryptedMessage getClaimOffer(@PathVariable String universityName, @PathVariable String userName, @PathVariable String provider, @PathVariable Long studentClaimId) {
         return getProvider(provider)
                 .map(service -> service.createClaimOffer(universityName, userName, studentClaimId))
                 .orElseThrow(() -> new IllegalStateException("Unknown claim provider."));
    }

    @PostMapping( "/{userName}/{provider}" )
    AuthcryptedMessage getClaim(@PathVariable String universityName, @PathVariable String userName, @RequestBody String provider, @RequestBody AuthcryptedMessage authcryptedMessage) {
        return getProvider(provider)
                .map(service -> service.getClaim(universityName, userName, authcryptedMessage))
                .orElseThrow(() -> new IllegalStateException("Unknown claim provider."));
    }
}
