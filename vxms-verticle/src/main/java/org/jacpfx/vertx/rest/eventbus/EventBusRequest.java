package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusRequest {

    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;

    public EventBusRequest(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
    }

    public  EventBusResponse send(String id, Object message) {
         return new EventBusResponse(vertx,t,errorMethodHandler,context,id,message, null, null);
    }
}
