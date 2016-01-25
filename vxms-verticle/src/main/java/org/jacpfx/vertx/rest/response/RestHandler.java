package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 07.01.16.
 */
public class RestHandler {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;

    public RestHandler(RoutingContext context, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler) {
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
        return new RSResponse(vertx,t,errorMethodHandler,context, new HashMap<>(), false);
    }

}
