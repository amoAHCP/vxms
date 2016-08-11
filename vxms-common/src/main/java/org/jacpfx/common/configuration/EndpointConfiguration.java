package org.jacpfx.common.configuration;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public interface EndpointConfiguration {

    /**
     * define a corse handler for your service
     * @param router
     */
    default void corsHandler(Router router) {

    }

    /**
     * Define a body handler for your service, a body handler is always set by default
     * @param router {@link Router}
     */
    default void bodyHandler(Router router) {
        router.route().handler(BodyHandler.create());
    }

    /**
     * Define a coockie handler for your service, a cookie handler is always defined by default
     * @param router {@link Router}
     */
    default void cookieHandler(Router router) {
        router.route().handler(CookieHandler.create());
    }

    /**
     * Define the static handler
     * @param router {@link Router}
     */
    default void staticHandler(Router router) {
    }

    /**
     * Define a Session handler
     * @param vertx {@link Vertx}
     * @param router {@link Router}
     */
    default void sessionHandler(Vertx vertx, Router router) {

    }

    default void customRouteConfiguration(Vertx vertx, Router router,boolean secure, String host, int port) {

    }
}
