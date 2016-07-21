package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the async createResponse chain.
 */
public class ExecuteWSObjectResponse extends ExecuteWSObject {

    protected ExecuteWSObjectResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, Serializable> errorHandlerObject, WebSocketRegistry registry, int retryCount, long timeout, long delay) {
        super(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount,timeout,delay);
    }

    /**
     * {@inheritDoc }
     */
    public ExecuteWSObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount, timeout, delay);
    }


    /**
     * {@inheritDoc }
     */
    public ExecuteWSObject onErrorResponse(Function<Throwable, Serializable> errorHandlerObject, Encoder encoder) {
        return new ExecuteWSObject(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * {@inheritDoc }
     */
    public ExecuteWSObjectResponse retry(int retryCount) {
        return new ExecuteWSObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the response chain
     */
    // TODO move timeout ot  ExecuteWSObject ... so onFailureRespond will be affected too
    public ExecuteWSObjectResponse timeout(long timeout) {
        return new ExecuteWSObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the response retries.
     *
     * @param delay
     * @return the response chain
     */
    public ExecuteWSObjectResponse delay(long delay) {
        return new ExecuteWSObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount, timeout, delay);
    }




}