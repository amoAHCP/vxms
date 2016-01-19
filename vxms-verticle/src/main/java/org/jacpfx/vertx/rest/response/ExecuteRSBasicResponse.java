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
public class ExecuteRSBasicResponse extends ExecuteRSBasic {


    public ExecuteRSBasicResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, ThrowableSupplier<String> stringSupplier, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, Function<Throwable, String> errorHandlerString, Function<Throwable, Serializable> errorHandlerObject, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerByte the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasic onByteResponseError(Function<Throwable, byte[]> errorHandlerByte) {
        return new ExecuteRSBasic(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerString the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasic onStringResponseError(Function<Throwable, String> errorHandlerString) {
        return new ExecuteRSBasic(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerObject the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasic onObjectResponseError(Function<Throwable, Serializable> errorHandlerObject, Encoder encoder) {
        return new ExecuteRSBasic(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    public ExecuteRSBasicResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    public ExecuteRSBasicResponse retry(int retryCount) {
        return new ExecuteRSBasicResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    public ExecuteRSBasic contentType(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return new ExecuteRSBasic(vertx, t, errorMethodHandler, context, headerMap, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    public ExecuteRSBasicResponse putHeader(String key,String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicResponse(vertx, t, errorMethodHandler, context, headerMap, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }
}
