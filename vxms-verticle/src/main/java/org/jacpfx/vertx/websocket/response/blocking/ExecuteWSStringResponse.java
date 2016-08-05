package org.jacpfx.vertx.websocket.response.blocking;

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
 * This class defines several error methods for the createResponse methods and executes the blocking createResponse chain.
 */
public class ExecuteWSStringResponse extends ExecuteWSString {

    public ExecuteWSStringResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<String> stringSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, String> errorHandlerString, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount,timeout,delay);
    }

    /**
     * {@inheritDoc }
     */
    public ExecuteWSStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }


    /**
     * {@inheritDoc }
     */
    public ExecuteWSStringResponse onFailureRespond(Function<Throwable, String> errorHandlerString) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }


    /**
     * {@inheritDoc }
     */
    public ExecuteWSStringResponse retry(int retryCount) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the response chain
     */
    public ExecuteWSStringResponse timeout(long timeout) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the response retries.
     *
     * @param delay
     * @return the response chain
     */
    public ExecuteWSStringResponse delay(long delay) {
        return new ExecuteWSStringResponse(endpoint, vertx, commType, stringSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerString, registry, retryCount, timeout, delay);
    }



}