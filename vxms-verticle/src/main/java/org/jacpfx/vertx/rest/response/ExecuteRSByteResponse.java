package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSByteResponse extends ExecuteRSByte {


    public ExecuteRSByteResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerByte the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSByte onErrorResponse(Function<Throwable, byte[]> errorHandlerByte) {
        return new ExecuteRSByte(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }

    public ExecuteRSByte contentType(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return new ExecuteRSByte(vertx, t, errorMethodHandler, context, headerMap, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }

    public ExecuteRSByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }

    public ExecuteRSByteResponse retry(int retryCount) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the response chain
     */
    public ExecuteRSByteResponse timeout(long timeout) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }

    /**
     * Defines the delay (in ms) between the response retries.
     *
     * @param delay
     * @return the response chain
     */
    public ExecuteRSByteResponse delay(long delay) {
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }


    public ExecuteRSByteResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headerMap, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }
}
