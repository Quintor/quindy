package nl.quintor.studybits.controllers.steward;

import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/steward")
public @interface StewardRestController {}