package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusAsyncRequest {
    protected final String methodId;
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final RoutingContext context;

    public EventBusAsyncRequest(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
    }


    public EventBusAsyncResponse send(String id, Object message) {
        return new EventBusAsyncResponse(methodId, vertx, t, errorMethodHandler, context, id, message, null);
    }
}
