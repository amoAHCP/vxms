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
 * Defines an event-bus request as the beginning of your (blocking) execution chain
 */
public class EventBusRequest {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;

    /**
     * Pass all members to execute the chain
     *
     * @param methodId           the method identifier
     * @param vertx              the vertx instance
     * @param failure            the vertx instance
     * @param errorMethodHandler the error-method handler
     * @param context            the vertx routing context
     */
    public EventBusRequest(String methodId,
                           Vertx vertx,
                           Throwable failure,
                           Consumer<Throwable> errorMethodHandler,
                           RoutingContext context) {
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.methodId = methodId;
    }

    /**
     * Send message and perform (blocking) task on reply
     *
     * @param targetId the target id to send to
     * @param message  the message to send
     * @return the execution chain {@link EventBusResponse}
     */
    public EventBusResponse send(String targetId, Object message) {
        return new EventBusResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                targetId,
                message,
                null);
    }

    /**
     * @param targetId the target id to send to
     * @param message  the message to send
     * @param options  the delivery options for sending the message
     * @return the execution chain {@link EventBusResponse}
     */
    public EventBusResponse send(String targetId, Object message, DeliveryOptions options) {
        return new EventBusResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                targetId,
                message,
                options);
    }

    /**
     * Quickreply, send message over event-bus and pass the result directly to rest response
     *
     * @param targetId the target id to send to
     * @param message  the message to send
     */
    public void sendAndRespondRequest(String targetId, Object message) {
        sendAndRespondRequest(targetId, message, new DeliveryOptions());
    }

    /**
     * Quickreply, send message over event-bus and pass the result directly to rest response
     *
     * @param targetId the target id to send to
     * @param message  the message to send
     * @param options  the event-bus delivery options
     */
    public void sendAndRespondRequest(String targetId, Object message, DeliveryOptions options) {
        vertx.eventBus().send(targetId, message, options != null ? options : new DeliveryOptions(), event -> {
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

    /**
     * Switch to blocking API
     *
     * @return the blocking chain {@link EventBusBlockingRequest}
     */
    public EventBusBlockingRequest blocking() {
        return new EventBusBlockingRequest(methodId, vertx, failure, errorMethodHandler, context);
    }
}
