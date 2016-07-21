package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;
import org.jacpfx.common.encoder.Encoder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByteResponse extends ExecuteRSBasicByte {


    public ExecuteRSBasicByteResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<byte[]> byteSupplier, ExecuteEventBusByteCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> onFailureRespond, int httpStatusCode, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteEventBusAndReply,encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSBasicByte onFailureRespond(Function<Throwable, byte[]> onFailureRespond) {
        return new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headers, byteSupplier,excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }

    /**
     * This is an intermediate error method, the error will be passed along the chain (onFailureRespond or simply an error)
     * @param errorHandler , a consumer that holds the error
     * @return the createResponse chain
     */
    public ExecuteRSBasicByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, byteSupplier,excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicByteResponse retry(int retryCount) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers,  byteSupplier,excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicByteResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headerMap, byteSupplier, excecuteEventBusAndReply,encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }
}
