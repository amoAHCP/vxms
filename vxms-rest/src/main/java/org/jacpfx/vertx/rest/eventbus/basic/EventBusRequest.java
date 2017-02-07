package org.jacpfx.vertx.rest.eventbus.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vertx.rest.eventbus.blocking.EventBusBlockingRequest;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusRequest {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;

    public EventBusRequest(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.methodId = methodId;
    }

    public EventBusResponse send(String id, Object message) {
        return new EventBusResponse(methodId, vertx, t, errorMethodHandler, context, id, message, null);
    }

    public EventBusResponse send(String id, Object message, DeliveryOptions options) {
        return new EventBusResponse(methodId, vertx, t, errorMethodHandler, context, id, message, options);
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
        } else if (resp instanceof JsonObject) {
            response.end(JsonObject.class.cast(resp).encode());
        } else if (resp instanceof JsonArray) {
            response.end(JsonArray.class.cast(resp).encode());
        }
    }

    public EventBusBlockingRequest blocking() {
        return new EventBusBlockingRequest(methodId, vertx, t, errorMethodHandler, context);
    }
}
