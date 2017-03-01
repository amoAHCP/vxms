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
 * This class defines the fluid API part to define the amount of time after the circuit breaker will be closed again
 */
public class ExecuteEventbusStringCircuitBreaker extends ExecuteEventbusStringResponse {


    /**
     * The constructor to pass all needed members
     *
     * @param methodId              the method identifier
     * @param vertx                 the vertx instance
     * @param failure               the failure thrown while task execution
     * @param errorMethodHandler    the error handler
     * @param message               the message to respond to
     * @param stringSupplier        the supplier, producing the byte response
     * @param errorHandler          the error handler
     * @param onFailureRespond      the consumer that takes a Future with the alternate response value in case of failure
     * @param deliveryOptions       the response deliver options
     * @param retryCount            the amount of retries before failure execution is triggered
     * @param timeout               the amount of time before the execution will be aborted
     * @param delay                 the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
     */
    public ExecuteEventbusStringCircuitBreaker(String methodId,
                                               Vertx vertx,
                                               Throwable failure,
                                               Consumer<Throwable> errorMethodHandler,
                                               Message<Object> message,
                                               ThrowableSupplier<String> stringSupplier,
                                               ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply,
                                               Consumer<Throwable> errorHandler,
                                               ThrowableFunction<Throwable, String> onFailureRespond,
                                               DeliveryOptions deliveryOptions,
                                               int retryCount,
                                               long timeout,
                                               long delay,
                                               long circuitBreakerTimeout) {
        super(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }


    /**
     * Define a timeout to release the stateful circuit breaker. Depending on your configuration the CircuitBreaker locks either cluster wide, jvm wide or only for the instance
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain {@link ExecuteEventbusStringResponse}
     */
    public ExecuteEventbusStringResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteEventbusStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }


}
