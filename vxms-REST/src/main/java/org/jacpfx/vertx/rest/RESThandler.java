package org.jacpfx.vertx.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import or.jacpfx.spi.RESThandlerSPI;
import org.jacpfx.vertx.rest.util.RESTInitializer;

/**
 * Created by amo on 05.08.16.
 */
public class RESThandler implements RESThandlerSPI {
    @Override
    public void initRESTHandler(Vertx vertx, Router router, JsonObject config, AbstractVerticle service) {
        RESTInitializer.initRESTHandler(vertx, router, config, service);
    }
}
