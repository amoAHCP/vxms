package org.jacpfx.vertx.event.response;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vertx.event.eventbus.basic.EventbusRequest;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 07.01.16.
 * The EventbusHandler gives access to the {@link RoutingContext} , the {@link org.jacpfx.vertx.event.response.EventbusRequest} , the {@link EventbusResponse} and the {@link EventbusRequest}.
 */
public class EventbusHandler {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> message;
    private final String methodId;

    public EventbusHandler(String methodId, Message<Object> message, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.message = message;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
    }


    public Message<Object> message() {
        return this.message;
    }

    public org.jacpfx.vertx.event.response.EventbusRequest request() {
        return new org.jacpfx.vertx.event.response.EventbusRequest(message);
    }

    public EventbusResponse response() {
        return new EventbusResponse(methodId, message, vertx, t, errorMethodHandler);
    }

    public EventbusRequest eventBusRequest() {
        return new EventbusRequest(methodId, message, vertx, t, errorMethodHandler);
    }

}
