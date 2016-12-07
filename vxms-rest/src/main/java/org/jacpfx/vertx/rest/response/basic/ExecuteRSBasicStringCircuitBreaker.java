package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicStringCircuitBreaker extends ExecuteRSBasicStringResponse {


    public ExecuteRSBasicStringCircuitBreaker(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<String> stringConsumer,
                                              ExecuteEventBusStringCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                              int httpStatusCode, int retryCount, long timeout,long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
    }




    /**
     * Define a timeout to release the stateful circuit breaker. Depending on your configuration the CircuitBreaker locks either cluster wide, jvm wide or only for the instance
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain
     */
    public ExecuteRSBasicStringResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
    }


}
