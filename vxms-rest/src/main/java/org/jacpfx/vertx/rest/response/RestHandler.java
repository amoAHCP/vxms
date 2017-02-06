package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vertx.rest.eventbus.basic.EventBusRequest;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 07.01.16.
 * The RestHandler gives access to the {@link RoutingContext} , the {@link RSRequest} , the {@link RSResponse} and the {@link EventBusRequest}.
 */
public class RestHandler {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String methodId;

    public RestHandler(String methodId, RoutingContext context, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.context = context;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
    }


    public RoutingContext context() {
        return this.context;
    }

    public RSRequest request() {
        return new RSRequest(context);
    }

    public RSResponse response() {
        return new RSResponse(methodId, vertx, t, errorMethodHandler, context, new HashMap<>());
    }

    public EventBusRequest eventBusRequest() {
        return new EventBusRequest(methodId, vertx, t, errorMethodHandler, context);
    }

}
