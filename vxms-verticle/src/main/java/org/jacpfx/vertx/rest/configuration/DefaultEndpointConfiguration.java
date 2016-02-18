package org.jacpfx.vertx.rest.configuration;

import io.vertx.ext.web.handler.*;

/**
 * Created by Andy Moncsek on 27.01.16.
 */
public class DefaultEndpointConfiguration implements EndpointConfiguration {

    // TODO Session handling needs a session store which mus be initilized by a vertx instance!!!

   // public void routeConfiguratipn() {
   //     Optional<CorsHandler> cHandler = Optional.ofNullable(getCorsHandler());
   // }


    @Override
    public CorsHandler corsHandler() {
        return null;
    }

    @Override
    public BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    @Override
    public CookieHandler cookieHandler() {
        return CookieHandler.create();
    }

    @Override
    public SessionHandler sessionHandler() {
        return null;
    }

    @Override
    public AuthHandler authHandler() {
        return null;
    }
}
