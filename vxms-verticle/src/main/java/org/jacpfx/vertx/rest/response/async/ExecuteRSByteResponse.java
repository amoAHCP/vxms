package org.jacpfx.vertx.rest.response.async;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCallAsync;
import org.jacpfx.common.encoder.Encoder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSByteResponse extends ExecuteRSByte {


    public ExecuteRSByteResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                 Map<String, String> headers, ThrowableSupplier<byte[]> byteSupplier, ExecuteEventBusByteCallAsync excecuteAsyncEventBusAndReply,
                                 Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers,  byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSByte onFailureRespond(Function<Throwable, byte[]> onFailureRespond) {
        return new ExecuteRSByte(vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply,encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }


    public ExecuteRSByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply,encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    public ExecuteRSByteResponse retry(int retryCount) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers,  byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse timeout(long timeout) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers,  byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries.
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse delay(long delay) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply,encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }


    public ExecuteRSByteResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headerMap, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }
}
