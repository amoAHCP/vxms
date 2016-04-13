package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicStringResponse extends ExecuteRSBasicString {


    public ExecuteRSBasicStringResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<String> stringSupplier, ExecuteEventBusStringCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerString the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasicString onErrorResponse(Function<Throwable, String> errorHandlerString) {
        return new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
    }


    public ExecuteRSBasicStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicStringResponse retry(int retryCount) {
        return new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicStringResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headerMap, stringSupplier, excecuteEventBusAndReply, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
    }
}
