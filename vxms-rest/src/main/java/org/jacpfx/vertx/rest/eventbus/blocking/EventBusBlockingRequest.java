package org.jacpfx.vertx.rest.eventbus.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusBlockingRequest {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;

    public EventBusBlockingRequest(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
    }


    public EventBusBlockingResponse send(String id, Object message) {
        return new EventBusBlockingResponse(methodId, vertx, t, errorMethodHandler, context, id, message, null);
    }
}