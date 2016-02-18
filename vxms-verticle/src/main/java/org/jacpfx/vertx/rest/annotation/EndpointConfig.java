package org.jacpfx.vertx.rest.annotation;

import org.jacpfx.vertx.rest.configuration.EndpointConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Andy Moncsek on 18.02.16. Define additional configurations like session, CORS, body handling. The config class must be
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EndpointConfig {
    /**
     * The Class of type EndpointConfiguration defining the custom Endpoint configuration
     * @return Class implementing EndpointConfiguration
     */
    Class<? extends EndpointConfiguration> value();
}
