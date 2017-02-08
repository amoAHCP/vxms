package org.jacpfx.vertx.rest.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation to define fallback methods for REST endpoints.
 * The value mus be the same (also the http method annotation POST,GET,...) as the REST method to identify the correct fallback. The marked method will be executed, in case of exceptions are not handled in the REST method
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnRestError {
    /**
     * The name/value of the method
     *
     * @return the value/identifier for the fakllback method
     */
    String value();
}
