package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.12.15.
 * Defines the type of the (async) response. This can be a byte, string or object response.
 */
public class ResponseTypeAsync extends ResponseType {


    protected ResponseTypeAsync(WebSocketEndpoint[] endpoint, Vertx vertx, final CommType commType, Consumer<Throwable> errorMethodHandler, WebSocketRegistry registry) {
        super(endpoint, vertx, commType, errorMethodHandler, registry);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteWSResponse(endpoint, vertx, commType, byteSupplier, null, null, null, null, errorMethodHandler, null, null, null, registry, 0, 0L, 0L);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteWSResponse(endpoint, vertx, commType, null, stringSupplier, null, null, null, errorMethodHandler, null, null, null, registry, 0, 0L, 0L);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteWSResponse(endpoint, vertx, commType, null, null, objectSupplier, encoder, null, errorMethodHandler, null, null, null, registry, 0, 0L, 0L);
    }
}