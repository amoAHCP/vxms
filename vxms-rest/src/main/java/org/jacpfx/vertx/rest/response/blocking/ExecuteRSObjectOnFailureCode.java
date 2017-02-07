package org.jacpfx.vertx.rest.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventBusObjectCallBlocking;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSObjectOnFailureCode extends ExecuteRSObject {


    public ExecuteRSObjectOnFailureCode(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<Serializable> objectSupplier, ExecuteEventBusObjectCallBlocking excecuteEventBusAndReply,
                                        Encoder encoder, Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, Serializable> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }


    /**
     * Define the HTTP Code in case of onFailure execution
     *
     * @param httpErrorCode the http error code to set for response, in case of error
     * @return the response chain
     */
    public ExecuteRSObject httpErrorCode(HttpResponseStatus httpErrorCode) {
        return new ExecuteRSObject(methodId, vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode.code(), retryCount, timeout, delay, circuitBreakerTimeout);
    }


}
