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
public class ExecuteEventbusObjectCircuitBreaker extends ExecuteEventbusObjectResponse {


    public ExecuteEventbusObjectCircuitBreaker(String methodId,
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
     * Defines how long a method can be executed before aborted.
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain
     */
    public ExecuteEventbusObjectResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteEventbusObjectResponse(methodId, vertx, t, errorMethodHandler, message, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


}
