package org.jacpfx.entity;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import org.jacpfx.vertx.rest.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class RestrictedBodyHandlingEndpointConfig implements EndpointConfiguration {
    public void corsHandler(Router router) {
        router.route().handler(CorsHandler.create("*").
                allowedMethod(io.vertx.core.http.HttpMethod.GET).
                allowedMethod(io.vertx.core.http.HttpMethod.POST).
                allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS).
                allowedMethod(io.vertx.core.http.HttpMethod.PUT).
                allowedMethod(io.vertx.core.http.HttpMethod.DELETE).
                allowedHeader("Content-Type").
                allowedHeader("X-Requested-With"));
    }

    public void bodyHandler(Router router) {

    }
}