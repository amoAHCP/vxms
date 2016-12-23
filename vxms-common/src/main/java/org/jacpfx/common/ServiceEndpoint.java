package org.jacpfx.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Andy Moncsek on 31.07.15. Defines an ServiceEndpoint and his metadata. E Class Annotated wit @ServiceEndpoint must extend from ServiceVerticle
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceEndpoint {

    /**
     * The port number to listen
     * @return The Endpoint Port
     */
    int port() default 8080;

    /**
     * The service name as identifier in distributed environments
     * @return the service name
     */
    String name() default "";



    String contextRoot() default "/";

    /**
     *
     * @return The host name to bind
     */
    String host() default "0.0.0.0";


    /**
     * Define custom http server options
     * @return  the server options
     */
    Class<? extends CustomServerOptions> options() default DefaultServerOptions.class;
}
