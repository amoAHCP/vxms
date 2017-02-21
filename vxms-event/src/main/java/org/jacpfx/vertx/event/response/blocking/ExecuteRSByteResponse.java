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
public class ExecuteRSByteResponse extends ExecuteRSByte {


    public ExecuteRSByteResponse(String methodId,
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
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSByte onFailureRespond(ThrowableFunction<Throwable, byte[]> onFailureRespond) {
        return new ExecuteRSByte(methodId, vertx, t, errorMethodHandler, message, byteSupplier, excecuteAsyncEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Will be executed on each error
     *
     * @param errorHandler
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, message, byteSupplier, excecuteAsyncEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * retry operation on error
     *
     * @param retryCount
     * @return the createResponse chain
     */
    public ExecuteRSByteCircuitBreaker retry(int retryCount) {
        return new ExecuteRSByteCircuitBreaker(methodId, vertx, t, errorMethodHandler, message, byteSupplier, excecuteAsyncEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse timeout(long timeout) {
        return new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, message, byteSupplier, excecuteAsyncEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries.
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse delay(long delay) {
        return new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, message, byteSupplier, excecuteAsyncEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

}
