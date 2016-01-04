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
 * Defines the type of the response. This can be a byte, string or object response.
 */
public class ResponseType {
    protected final WebSocketEndpoint[] endpoint;
    protected final Vertx vertx;
    protected final CommType commType;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final WebSocketRegistry registry;

    protected ResponseType(WebSocketEndpoint[] endpoint, Vertx vertx, final CommType commType, Consumer<Throwable> errorMethodHandler, WebSocketRegistry registry) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.commType = commType;
        this.errorMethodHandler = errorMethodHandler;
        this.registry = registry;
    }

    /**
     * Retunrs a byte array to the target type
     *
     * @param byteSupplier supplier which returns the response value as byte array
     * @return @see{org.jacpfx.vertx.websocket.response.ExecuteWSBasicResponse}
     */
    public ExecuteWSBasicResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, null, null, null, null, errorMethodHandler, null, null, null, registry, 0);
    }

    /**
     * Retunrs a String to the target type
     *
     * @param stringSupplier supplier which returns the response value as String
     * @return @see{org.jacpfx.vertx.websocket.response.ExecuteWSBasicResponse}
     */
    public ExecuteWSBasicResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, null, stringSupplier, null, null, null, errorMethodHandler, null, null, null, registry, 0);
    }

    /**
     * Retunrs a Serializable to the target type
     *
     * @param objectSupplier supplier which returns the response value as Serializable
     * @return @see{org.jacpfx.vertx.websocket.response.ExecuteWSBasicResponse}
     */
    public ExecuteWSBasicResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, null, null, objectSupplier, encoder, null, errorMethodHandler, null, null, null, registry, 0);
    }
}