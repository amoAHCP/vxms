package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.response.basic.ExecuteWSBasicByteResponse;
import org.jacpfx.vertx.websocket.response.basic.ExecuteWSBasicObjectResponse;
import org.jacpfx.vertx.websocket.response.basic.ExecuteWSBasicStringResponse;
import org.jacpfx.vertx.websocket.util.CommType;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.12.15.
 * Defines the type of the createResponse. This can be a byte, string or object createResponse.
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
     * @param byteSupplier supplier which returns the createResponse value as byte array
     * @return @see{org.jacpfx.vertx.websocket.createResponse.ExecuteWSBasicResponse}
     */
    public ExecuteWSBasicByteResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteWSBasicByteResponse(endpoint, vertx, commType, byteSupplier, null, null, errorMethodHandler, null, registry, 0);
    }

    /**
     * Retunrs a String to the target type
     *
     * @param stringSupplier supplier which returns the createResponse value as String
     * @return @see{org.jacpfx.vertx.websocket.createResponse.ExecuteWSBasicResponse}
     */
    public ExecuteWSBasicStringResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteWSBasicStringResponse(endpoint, vertx, commType, stringSupplier, null, null, errorMethodHandler, null, registry, 0);
    }

    /**
     * Retunrs a Serializable to the target type
     *
     * @param objectSupplier supplier which returns the createResponse value as Serializable
     * @return @see{org.jacpfx.vertx.websocket.createResponse.ExecuteWSBasicResponse}
     */
    public ExecuteWSBasicObjectResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteWSBasicObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, null, errorMethodHandler, null, registry, 0);
    }
}