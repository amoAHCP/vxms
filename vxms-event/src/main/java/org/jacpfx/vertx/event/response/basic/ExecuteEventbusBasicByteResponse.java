package org.jacpfx.vertx.event.response.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusByteCall;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteEventbusBasicByteResponse extends ExecuteEventbusBasicByte {


    public ExecuteEventbusBasicByteResponse(String methodId,
                                            Vertx vertx,
                                            Throwable t,
                                            Consumer<Throwable> errorMethodHandler,
                                            Message<Object> message,
                                            ThrowableFutureConsumer<byte[]> byteConsumer,
                                            ExecuteEventbusByteCall excecuteEventBusAndReply,
                                            Consumer<Throwable> errorHandler,
                                            ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                            DeliveryOptions deliveryOptions,
                                            int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteEventbusBasicByteResponse onFailureRespond(ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond) {
        return new ExecuteEventbusBasicByteResponse(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * This is an intermediate error method, the error will be passed along the chain (onFailurePass or simply an error)
     *
     * @param errorHandler , a consumer that holds the error
     * @return the response chain
     */
    public ExecuteEventbusBasicByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteEventbusBasicByteResponse(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout
     * @return
     */

    public ExecuteEventbusBasicByteResponse timeout(long timeout) {
        return new ExecuteEventbusBasicByteResponse(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply,  errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * retry execution N times before
     *
     * @param retryCount the amount of retries
     * @return the response chain
     */
    public ExecuteEventbusBasicByteCircuitBreaker retry(int retryCount) {
        return new ExecuteEventbusBasicByteCircuitBreaker(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply,  errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }

}
