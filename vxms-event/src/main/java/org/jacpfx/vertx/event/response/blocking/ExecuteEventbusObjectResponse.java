package org.jacpfx.vertx.event.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusObjectCallBlocking;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusObjectResponse extends ExecuteEventbusObject {


    public ExecuteEventbusObjectResponse(String methodId,
                                         Vertx vertx, Throwable t,
                                         Consumer<Throwable> errorMethodHandler,
                                         Message<Object> message,
                                         ThrowableSupplier<Serializable> objectSupplier,
                                         ExecuteEventbusObjectCallBlocking excecuteEventBusAndReply,
                                         Encoder encoder,
                                         Consumer<Throwable> errorHandler,
                                         ThrowableFunction<Throwable, Serializable> onFailureRespond,
                                         DeliveryOptions deliveryOptions, int retryCount,
                                         long timeout, long delay, long circuitBreakerTimeout){

            super(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteEventbusObject onFailureRespond(ThrowableFunction<Throwable, Serializable> onFailureRespond, Encoder encoder) {
        return new ExecuteEventbusObject(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Will be executed on each error
     *
     * @param errorHandler
     * @return the createResponse chain
     */
    public ExecuteEventbusObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteEventbusObjectResponse(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * retry operation on error
     *
     * @param retryCount
     * @return the createResponse chain
     */
    public ExecuteEventbusObjectCircuitBreaker retry(int retryCount) {
        return new ExecuteEventbusObjectCircuitBreaker(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteEventbusObjectResponse timeout(long timeout) {
        return new ExecuteEventbusObjectResponse(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries (on error).
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteEventbusObjectResponse delay(long delay) {
        return new ExecuteEventbusObjectResponse(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


}
