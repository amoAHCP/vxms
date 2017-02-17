package org.jacpfx.vertx.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import or.jacpfx.spi.RESThandlerSPI;

/**
 * Created by amo on 05.08.16.
 * Implements teh RESThandlerSPI and calls the initializer to bootstrap the rest API
 */
public class RESThandler implements RESThandlerSPI {
    @Override
    public void initRESTHandler(Vertx vertx, Router router,  AbstractVerticle service) {
        RESTInitializer.initRESTHandler(vertx, router, service);
    }
}
