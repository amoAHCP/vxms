package org.jacpfx.vertx.event;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import or.jacpfx.spi.EventhandlerSPI;

/**
 * Created by amo on 05.08.16.
 */
public class Eventhandler implements EventhandlerSPI {
    @Override
    public void initEventHandler(Vertx vertx,  AbstractVerticle service) {
        EventInitializer.initEventbusHandling(vertx,  service);
    }
}
