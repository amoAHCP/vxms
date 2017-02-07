package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.basic.ExecuteEventBusByteCall;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByteOnFailureCode extends ExecuteRSBasicByteResponse {


    public ExecuteRSBasicByteOnFailureCode(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<byte[]> byteConsumer, ExecuteEventBusByteCall excecuteEventBusAndReply, Encoder encoder,
                                           Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }


    /**
     * Define the HTTP Code in case of onFailure execution
     *
     * @param httpErrorCode the http error code to set for response, in case of error
     * @return the response chain
     */
    public ExecuteRSBasicByteResponse httpErrorCode(HttpResponseStatus httpErrorCode) {
        return new ExecuteRSBasicByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode.code(), retryCount, timeout, circuitBreakerTimeout);
    }


}
