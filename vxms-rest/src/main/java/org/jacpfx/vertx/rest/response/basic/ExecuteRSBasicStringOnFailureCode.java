package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.basic.ExecuteEventBusStringCall;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicStringOnFailureCode extends ExecuteRSBasicStringResponse {


    public ExecuteRSBasicStringOnFailureCode(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<String> stringConsumer,
                                             ExecuteEventBusStringCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                             int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }


    /**
     * Define the HTTP Code in case of onFailure execution
     *
     * @param httpErrorCode the http error code to set for response, in case of error
     * @return the response chain
     */
    public ExecuteRSBasicStringResponse httpErrorCode(HttpResponseStatus httpErrorCode) {
        return new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode.code(), retryCount, timeout, circuitBreakerTimeout);
    }


}
