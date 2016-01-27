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
public class ExecuteRSBasicObjectResponse extends ExecuteRSBasicObject {


    public ExecuteRSBasicObjectResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerObject the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasicObject onErrorResponse(Function<Throwable, Serializable> errorHandlerObject, Encoder encoder) {
        return new ExecuteRSBasicObject(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicObjectResponse retry(int retryCount) {
        return new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicObject contentType(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return new ExecuteRSBasicObject(vertx, t, errorMethodHandler, context, headerMap, async, objectSupplier, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicObjectResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headerMap, async, objectSupplier, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount);
    }
}
