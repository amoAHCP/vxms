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
    public ExecuteWSByteResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteWSByteResponse(endpoint, vertx, commType, byteSupplier, null, null, errorMethodHandler, null, registry, 0, 0L, 0L);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSStringResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, null, null, errorMethodHandler, null, registry, 0, 0L, 0L);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExecuteWSObjectResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteWSObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, null, errorMethodHandler, null, registry, 0, 0L, 0L);
    }
}