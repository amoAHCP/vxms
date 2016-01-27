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
public class ExecuteRSBasicByteResponse extends ExecuteRSBasicByte {


    public ExecuteRSBasicByteResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerByte the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasicByte onErrorResponse(Function<Throwable, byte[]> errorHandlerByte) {
        return new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
    }


    public ExecuteRSBasicByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicByteResponse retry(int retryCount) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicByte contentType(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headerMap, async, byteSupplier, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicByteResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headerMap, async, byteSupplier, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
    }
}
