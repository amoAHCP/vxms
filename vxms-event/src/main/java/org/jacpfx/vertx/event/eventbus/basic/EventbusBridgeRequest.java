package org.jacpfx.vertx.event.eventbus.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jacpfx.vertx.event.eventbus.blocking.EventbusBridgeBlockingRequest;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventbusBridgeRequest {
    private final String methodId;
    private final Message<Object> requestmessage;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;

    public EventbusBridgeRequest(String methodId, Message<Object> requestmessage, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.requestmessage = requestmessage;
        this.methodId = methodId;
    }

    public EventbusBridgeResponse send(String id, Object message) {
        return new EventbusBridgeResponse(methodId, requestmessage, vertx, t, errorMethodHandler, id, message, null);
    }

    public EventbusBridgeResponse send(String id, Object message, DeliveryOptions requestOptions) {
        return new EventbusBridgeResponse(methodId, requestmessage, vertx, t, errorMethodHandler, id, message, requestOptions);
    }


    public void sendAndRespondRequest(String id, Object message) {
        sendAndRespondRequest(id, message, new DeliveryOptions());
    }

    public void sendAndRespondRequest(String id, Object message, DeliveryOptions requestOptions) {
        vertx.eventBus().send(id, message, requestOptions != null ? requestOptions : new DeliveryOptions(), event -> {
            if (event.failed()) {
                requestmessage.fail(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), event.cause().getMessage());
            }
            Optional.ofNullable(event.result()).ifPresent(result -> Optional.ofNullable(result.body()).ifPresent(resp -> respond(resp, requestOptions)));
        });
    }

    protected void respond(Object resp, DeliveryOptions options) {
        if (resp instanceof String) {
            if (options != null) {
                requestmessage.reply((String) resp, options);
            } else {
                requestmessage.reply((String) resp);
            }
        } else if (resp instanceof byte[]) {
            if (options != null) {
                requestmessage.reply(Buffer.buffer((byte[]) resp), options);
            } else {
                requestmessage.reply(Buffer.buffer((byte[]) resp));
            }
        } else if (resp instanceof JsonObject) {
            if (options != null) {
                requestmessage.reply(JsonObject.class.cast(resp).encode(),options);
            } else {
                requestmessage.reply(JsonObject.class.cast(resp).encode());
            }
        } else if (resp instanceof JsonArray) {
            if (options != null) {
                requestmessage.reply(JsonArray.class.cast(resp).encode(),options);
            } else {
                requestmessage.reply(JsonArray.class.cast(resp).encode());
            }
        }
    }

    public EventbusBridgeBlockingRequest blocking() {
        return new EventbusBridgeBlockingRequest(methodId, requestmessage, vertx, t, errorMethodHandler);
    }
}
