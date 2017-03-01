package org.jacpfx.vertx.event.response.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusStringCall;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusBasicStringResponse extends ExecuteEventbusBasicString {


    public ExecuteEventbusBasicStringResponse(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, Message<Object> message,
                                              ThrowableFutureConsumer<String> stringConsumer, ExecuteEventbusStringCall excecuteEventBusAndReply, Consumer<Throwable> errorHandler,
                                              ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                              DeliveryOptions deliveryOptions, int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, message, stringConsumer, excecuteEventBusAndReply, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }


    /**
     * intermediate error handler which will be called on each error (at least 1 time, in case on N retries... up to N times)
     *
     * @param errorHandler the handler to be executed on each error
     * @return the response chain
     */
    public ExecuteEventbusBasicStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteEventbusBasicStringResponse(methodId, vertx, failure, errorMethodHandler, message, stringConsumer, excecuteEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout the amount of timeout in ms
     * @return the response chain
     */
    public ExecuteEventbusBasicStringResponse timeout(long timeout) {
        return new ExecuteEventbusBasicStringResponse(methodId, vertx, failure, errorMethodHandler, message, stringConsumer, excecuteEventBusAndReply,  errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * retry execution N times before
     *
     * @param retryCount the amount of retries
     * @return the response chain
     */
    public ExecuteEventbusBasicStringCircuitBreaker retry(int retryCount) {
        return new ExecuteEventbusBasicStringCircuitBreaker(methodId, vertx, failure, errorMethodHandler, message, stringConsumer, excecuteEventBusAndReply, errorHandler, onFailureRespond,
                deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value, this handler is a terminal handler and will be executed only once
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteEventbusBasicStringResponse onFailureRespond(ThrowableErrorConsumer<Throwable, String> onFailureRespond) {
        return new ExecuteEventbusBasicStringResponse(methodId, vertx, failure, errorMethodHandler, message, stringConsumer, excecuteEventBusAndReply, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }


}
