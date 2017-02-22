package org.jacpfx.vertx.event.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusByteCallBlocking;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusByteCircuitBreaker extends ExecuteEventbusByteResponse {


    public ExecuteEventbusByteCircuitBreaker(String methodId,
                                             Vertx vertx,
                                             Throwable t,
                                             Consumer<Throwable> errorMethodHandler,
                                             Message<Object> message,
                                             ThrowableSupplier<byte[]> byteSupplier,
                                             ExecuteEventbusByteCallBlocking excecuteAsyncEventBusAndReply,
                                             Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond,
                                             DeliveryOptions deliveryOptions,
                                             int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, message, byteSupplier, excecuteAsyncEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain
     */
    public ExecuteEventbusByteResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteEventbusByteResponse(methodId, vertx, t, errorMethodHandler, message, byteSupplier, excecuteAsyncEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

}
