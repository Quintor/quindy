package nl.quintor.studybits.university;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor( onConstructor = @__( @Autowired ) )
public class RequestInterceptor extends HandlerInterceptorAdapter {

    private final UserContext userContext;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object object) {
        try {
            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String universityName = (String) pathVariables.get("universityName");
            String userName = (String) pathVariables.get("userName");
            userContext.setCurrentUser(universityName, userName);
            log.debug("request to university {} from user {}.", universityName, userName);
        } catch (Exception e) {
            log.error("Request did not have university and user context", e);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView model) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object object, Exception arg3) {
        userContext.setCurrentUser(null, null);
    }
}