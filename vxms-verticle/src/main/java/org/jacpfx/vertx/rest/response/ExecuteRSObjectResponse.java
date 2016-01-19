package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSObjectResponse extends ExecuteRSObject {


    public ExecuteRSObjectResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerObject the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSObject onErrorResponse(Function<Throwable, Serializable> errorHandlerObject, Encoder encoder) {
        return new ExecuteRSObject(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }

    public ExecuteRSObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }

    public ExecuteRSObjectResponse retry(int retryCount) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the response chain
     */
    public ExecuteRSObjectResponse timeout(long timeout) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }

    /**
     * Defines the delay (in ms) between the response retries (on error).
     *
     * @param delay
     * @return the response chain
     */
    public ExecuteRSObjectResponse delay(long delay) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }

    public ExecuteRSObject contentType(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return new ExecuteRSObject(vertx, t, errorMethodHandler, context, headerMap, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }

    public ExecuteRSObjectResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headerMap, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }
}
