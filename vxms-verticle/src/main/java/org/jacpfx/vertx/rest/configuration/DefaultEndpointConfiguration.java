package org.jacpfx.vertx.rest.configuration;

import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.Optional;

/**
 * Created by Andy Moncsek on 27.01.16.
 */
public class DefaultEndpointConfiguration {

    // TODO Session handling needs a session store which mus be initilized by a vertx instance!!!

    public void routeConfiguratipn() {
        Optional<CorsHandler> cHandler = Optional.ofNullable(getCorsHandler());
    }


    public CorsHandler getCorsHandler(){
        return null;
    }

    public BodyHandler getBodyHandler() {
        return BodyHandler.create();
    }

    public CookieHandler getCookieHandler() {
        return CookieHandler.create();
    }

    public AuthHandler getAuthHandler() {
        return null;
    }
}
