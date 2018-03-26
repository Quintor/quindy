package nl.quintor.studybits.university;

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
public class RequestInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private UserContext userContext;

    /**
     * This is not a good practice to use sysout. Always integrate any logger
     * with your application. We will discuss about integrating logger with
     * spring boot application in some later article
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object object) throws Exception {


        try {
            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String universityName = (String) pathVariables.get("universityName");
            String userName = (String) pathVariables.get("userName");
            //String universityName = ServletRequestUtils.getStringParameter(request, "universityName");
            //String userName = ServletRequestUtils.getStringParameter(request, "userName");
            userContext.setCurrentUser(universityName, userName);
            log.info("request to university {} from user {}.", universityName, userName);
        }
        catch (Exception e) {
            log.warn("Request did not have university and user context");
            return true;
        }


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object object, ModelAndView model)
            throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object object, Exception arg3)
            throws Exception {

    }
}