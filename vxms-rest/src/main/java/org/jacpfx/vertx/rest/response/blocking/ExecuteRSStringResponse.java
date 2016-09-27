package org.jacpfx.vertx.rest.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCallAsync;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSStringResponse extends ExecuteRSString {


    public ExecuteRSStringResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<String> stringSupplier, ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler,
                                   Function<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteAsyncEventBusAndReply,encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSString onFailureRespond(Function<Throwable, String> onFailureRespond) {
        return new ExecuteRSString(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }


    public ExecuteRSStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * retry operation on error
     * @param retryCount
     * @return the createResponse chain
     */
    public ExecuteRSStringResponse retry(int retryCount) {
        return new ExecuteRSStringResponse(vertx, t, errorMethodHandler, context, headers,  stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteRSStringResponse timeout(long timeout) {
        return new ExecuteRSStringResponse(vertx, t, errorMethodHandler, context, headers,  stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries (on error).
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteRSStringResponse delay(long delay) {
        return new ExecuteRSStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }


    public ExecuteRSStringResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSStringResponse(vertx, t, errorMethodHandler, context, headerMap, stringSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

}
