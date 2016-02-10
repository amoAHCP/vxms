package org.jacpfx.vertx.rest.configuration;

import io.vertx.ext.web.handler.*;

/**
 * Created by Andy Moncsek on 27.01.16.
 */
public class DefaultEndpointConfiguration {

    // TODO Session handling needs a session store which mus be initilized by a vertx instance!!!

   // public void routeConfiguratipn() {
   //     Optional<CorsHandler> cHandler = Optional.ofNullable(getCorsHandler());
   // }


    public CorsHandler getCorsHandler(String uri) {
        return null;
    }

    public BodyHandler getBodyHandler(String uri) {
        return BodyHandler.create();
    }

    public CookieHandler getCookieHandler(String uri) {
        return CookieHandler.create();
    }

    public SessionHandler getSessionHandler(String uri) {
        return null;
    }

    public AuthHandler getAuthHandler() {
        return null;
    }
}
