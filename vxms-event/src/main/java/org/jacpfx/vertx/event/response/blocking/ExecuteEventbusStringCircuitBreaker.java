package org.jacpfx.vertx.event.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusStringCallBlocking;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusStringCircuitBreaker extends ExecuteEventbusStringResponse {


    public ExecuteEventbusStringCircuitBreaker(String methodId,
                                               Vertx vertx,
                                               Throwable t,
                                               Consumer<Throwable> errorMethodHandler,
                                               Message<Object> message,
                                               ThrowableSupplier<String> stringSupplier,
                                               ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply,
                                               Consumer<Throwable> errorHandler,
                                               ThrowableFunction<Throwable, String> onFailureRespond,
                                               DeliveryOptions deliveryOptions,
                                               int retryCount, long timeout,
                                               long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t,
                errorMethodHandler,
                message,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay, circuitBreakerTimeout);
    }


    /**
     * Define a timeout to release the stateful circuit breaker. Depending on your configuration the CircuitBreaker locks either cluster wide, jvm wide or only for the instance
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain
     */
    public ExecuteEventbusStringResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteEventbusStringResponse(methodId, vertx, t, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


}
