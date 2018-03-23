package nl.quintor.studybits.university.services;

import nl.quintor.studybits.indy.wrapper.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IssuerService {

    private Map<String, Issuer> issuers;

    @Autowired
    public IssuerService( Issuer[] issuers ) {
        this.issuers = Arrays.stream(issuers)
                             .collect(Collectors.toMap(x -> x.getName()
                                                             .toLowerCase(), x -> x));
    }

    public Optional<Issuer> findIssuer( String universityName ) {
        Issuer issuer = issuers.get(universityName.toLowerCase());
        return Optional.ofNullable(issuer);
    }

    public Issuer getIssuer( String universityName ) {
        return findIssuer(universityName).orElseThrow(() -> new IllegalArgumentException(String.format("Issuer not found for university '%s'.", universityName)));
    }


}
