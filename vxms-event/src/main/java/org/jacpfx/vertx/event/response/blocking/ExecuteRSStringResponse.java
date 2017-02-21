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
public class ExecuteRSStringResponse extends ExecuteRSString {


    public ExecuteRSStringResponse(String methodId,
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
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSString onFailureRespond(ThrowableFunction<Throwable, String> onFailureRespond) {
        return new ExecuteRSString(methodId, vertx, t, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


    public ExecuteRSStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSStringResponse(methodId, vertx, t, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * retry operation on error
     *
     * @param retryCount
     * @return the createResponse chain
     */
    public ExecuteRSStringCircuitBreaker retry(int retryCount) {
        return new ExecuteRSStringCircuitBreaker(methodId, vertx, t, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteRSStringResponse timeout(long timeout) {
        return new ExecuteRSStringResponse(methodId, vertx, t, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries (on error).
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteRSStringResponse delay(long delay) {
        return new ExecuteRSStringResponse(methodId, vertx, t, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


}
