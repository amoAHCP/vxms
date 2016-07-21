package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the async createResponse chain.
 */
public class ExecuteWSByteResponse extends ExecuteWSByte {


    protected ExecuteWSByteResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, byte[]> errorHandlerByte, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, byteSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, registry, retryCount,timeout,delay);
    }

    /**
     * {@inheritDoc }
     */
    public ExecuteWSByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSByteResponse(endpoint, vertx, commType, byteSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, registry, retryCount, timeout, delay);
    }

    /**
     * {@inheritDoc }
     */
    public ExecuteWSByte onErrorResponse(Function<Throwable, byte[]> errorHandlerByte) {
        return new ExecuteWSByte(endpoint, vertx, commType, byteSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, registry, retryCount, timeout, delay);
    }


    /**
     * {@inheritDoc }
     */
    public ExecuteWSByteResponse retry(int retryCount) {
        return new ExecuteWSByteResponse(endpoint, vertx, commType, byteSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, registry, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the response chain
     */
    public ExecuteWSByteResponse timeout(long timeout) {
        return new ExecuteWSByteResponse(endpoint, vertx, commType, byteSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, registry, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the response retries.
     *
     * @param delay
     * @return the response chain
     */
    public ExecuteWSByteResponse delay(long delay) {
        return new ExecuteWSByteResponse(endpoint, vertx, commType, byteSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, registry, retryCount, timeout, delay);
    }


}