package nl.quintor.studybits.university.helpers;

import nl.quintor.studybits.university.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.hateoas.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
public class LinkHelper {

    @Autowired
    private UserContext userContext;

    public Map<String, Object> getIdentityPathVariables() {
        return userContext.getCurrentUser().map(identity -> {
            HashMap<String, Object> arguments = new HashMap<>();
            arguments.put("universityName", identity.getUniversityName());
            arguments.put("userName", identity.getUserName());
            return arguments;
        }).orElseGet(() -> new HashMap<>());
    }


    public <T extends ResourceSupport, U> T withLink(T resource, Class<U> controllerType, Function<U, ?> proxyInvocation) {
        Object invocationValue = proxyInvocation.apply(methodOn(controllerType));
        Link link = linkTo(invocationValue)
                .withSelfRel()
                .expand(getIdentityPathVariables());
        resource.add(link);
        return resource;
    }

}
