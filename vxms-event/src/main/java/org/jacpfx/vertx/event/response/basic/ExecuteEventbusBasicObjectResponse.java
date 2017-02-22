package org.jacpfx.vertx.event.response.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusObjectCall;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusBasicObjectResponse extends ExecuteEventbusBasicObject {


    public ExecuteEventbusBasicObjectResponse(String methodId,
                                              Vertx vertx, Throwable t,
                                              Consumer<Throwable> errorMethodHandler,
                                              Message<Object> message,
                                              ThrowableFutureConsumer<Serializable> objectConsumer,
                                              ExecuteEventbusObjectCall excecuteEventBusAndReply,
                                              Encoder encoder,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond,
                                              DeliveryOptions deliveryOptions,
                                              int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, message, objectConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteEventbusBasicObjectResponse onFailureRespond(ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond, Encoder encoder) {
        return new ExecuteEventbusBasicObjectResponse(methodId, vertx, t, errorMethodHandler, message, objectConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, deliveryOptions,retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * intermediate error handler which will be called on each error (at least 1 time, in case on N retries... up to N times)
     *
     * @param errorHandler the handler to be executed on each error
     * @return the response chain
     */
    public ExecuteEventbusBasicObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteEventbusBasicObjectResponse(methodId, vertx, t, errorMethodHandler, message, objectConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, deliveryOptions,retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout the amount of timeout in ms
     * @return the response chain
     */
    public ExecuteEventbusBasicObjectResponse timeout(long timeout) {
        return new ExecuteEventbusBasicObjectResponse(methodId, vertx, t, errorMethodHandler, message,objectConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, deliveryOptions,retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * retry execution N times before
     *
     * @param retryCount the amount of retries
     * @return the response chain
     */
    public ExecuteEventbusBasicObjectCircuitBreaker retry(int retryCount) {
        return new ExecuteEventbusBasicObjectCircuitBreaker(methodId, vertx, t, errorMethodHandler, message, objectConsumer, excecuteEventBusAndReply, encoder,
                errorHandler, onFailureRespond, deliveryOptions,retryCount, timeout, circuitBreakerTimeout);
    }


}
