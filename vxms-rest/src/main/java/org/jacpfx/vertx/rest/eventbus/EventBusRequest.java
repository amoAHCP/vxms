package org.jacpfx.vertx.rest.eventbus;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;
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

    public EventBusResponse send(String id, Object message) {
        return new EventBusResponse(vertx, t, errorMethodHandler, context, id, message, null, null);
    }


    public void sendAndRespondRequest(String id, Object message) {
        sendAndRespondRequest(id, message, new DeliveryOptions());
    }

    public void sendAndRespondRequest(String id, Object message, DeliveryOptions options) {
        vertx.eventBus().send(id, message, options != null ? options : new DeliveryOptions(), event -> {
            final HttpServerResponse response = context.response();
            if (event.failed()) {
                response.setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end();
            }
            Optional.ofNullable(event.result()).ifPresent(result -> Optional.ofNullable(result.body()).ifPresent(resp -> respond(response, resp)));
        });
    }

    protected void respond(HttpServerResponse response, Object resp) {
        if (resp instanceof String) {
            response.end((String) resp);
        } else if (resp instanceof byte[]) {

            response.end(Buffer.buffer((byte[]) resp));
        } else {
            // TODO implement object Response
           // WebSocketExecutionUtil.encode(resp, encoder).ifPresent(value -> RESTExecutionUtil.sendObjectResult(value, context.createResponse()));
        }
    }

    public EventBusAsyncRequest async() {
        return new EventBusAsyncRequest(vertx, t, errorMethodHandler, context);
    }
}
