package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCall;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicObjectResponse extends ExecuteRSBasicObject {


    public ExecuteRSBasicObjectResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<Serializable> objectSupplier, ExecuteEventBusObjectCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSBasicObject onFailureRespond(Function<Throwable, Serializable> onFailureRespond, Encoder encoder) {
        return new ExecuteRSBasicObject(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicObjectResponse retry(int retryCount) {
        return new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }

    public ExecuteRSBasicObjectResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headerMap, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
    }
}
