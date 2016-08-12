package org.jacpfx.vxms.petshop;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.jacpfx.common.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 07.07.16.
 */
public class CustomEndpointConfiguration implements EndpointConfiguration {
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

    @Override
    public void staticHandler(Router router) {
        router.route().handler(StaticHandler.create());
    }
}
