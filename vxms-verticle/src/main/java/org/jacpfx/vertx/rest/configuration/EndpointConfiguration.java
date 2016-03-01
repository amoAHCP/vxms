package org.jacpfx.vertx.rest.configuration;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public interface EndpointConfiguration {
    default CorsHandler corsHandler() {
        return null;
    }

    default BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    default CookieHandler cookieHandler() {
        return CookieHandler.create();
    }

    default StaticHandler staticHandler() {
        return null;
    }

    default SessionHandler sessionHandler() {
        return null;
    }

    default AuthHandler authHandler() {
        return null;
    }

    default void customRouteConfiguration(Vertx vertx, Router router) {

    }
}
