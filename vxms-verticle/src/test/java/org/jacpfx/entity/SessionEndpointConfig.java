package org.jacpfx.entity;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.jacpfx.common.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class SessionEndpointConfig implements EndpointConfiguration {


    public void sessionHandler(Vertx vertx, Router router) {
        // Create a clustered session store using defaults
        SessionStore store = LocalSessionStore.create(vertx, "xyz");

        SessionHandler sessionHandler = SessionHandler.create(store);

        // Make sure all requests are routed through the session handler too
        router.route().handler(sessionHandler);

    }

    public BodyHandler bodyHandler() {
        return BodyHandler.create();
    }
}