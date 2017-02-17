package org.jacpfx.vertx.rest.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventBusStringCallBlocking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API for byte responses, defines access to failure handling, timeouts,...
 */
public class ExecuteRSStringResponse extends ExecuteRSString {

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                      the method identifier
     * @param vertx                         the vertx instance
     * @param failure                       the failure thrown while task execution
     * @param errorMethodHandler            the error handler
     * @param context                       the vertx routing context
     * @param headers                       the headers to pass to the response
     * @param stringSupplier                the supplier, producing the byte response
     * @param excecuteBlockingEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                       the encoder to encode your objects
     * @param errorHandler                  the error handler
     * @param onFailureRespond              the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode                the http status code to set for response
     * @param httpErrorCode                 the http error code to set in case of failure handling
     * @param retryCount                    the amount of retries before failure execution is triggered
     * @param timeout                       the amount of time before the execution will be aborted
     * @param delay                         the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout         the amount of time before the circuit breaker closed again
     */
    public ExecuteRSStringResponse(String methodId,
                                   Vertx vertx,
                                   Throwable failure,
                                   Consumer<Throwable> errorMethodHandler,
                                   RoutingContext context,
                                   Map<String, String> headers,
                                   ThrowableSupplier<String> stringSupplier,
                                   ExecuteEventBusStringCallBlocking excecuteBlockingEventBusAndReply,
                                   Encoder encoder,
                                   Consumer<Throwable> errorHandler,
                                   ThrowableFunction<Throwable, String> onFailureRespond,
                                   int httpStatusCode,
                                   int httpErrorCode,
                                   int retryCount,
                                   long timeout,
                                   long delay,
                                   long circuitBreakerTimeout) {
        super(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteBlockingEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain {@link ExecuteRSStringOnFailureCode}
     */
    public ExecuteRSStringOnFailureCode onFailureRespond(ThrowableFunction<Throwable, String> onFailureRespond) {
        return new ExecuteRSStringOnFailureCode(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * intermediate error handler which will be called on each error (at least 1 time, in case on N retries... up to N times)
     *
     * @param errorHandler the handler to be executed on each error
     * @return the response chain {@link ExecuteRSStringResponse}
     */
    public ExecuteRSStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * retry operation on error
     *
     * @param retryCount the amount of retries before failure
     * @return the createResponse chain {@link ExecuteRSStringCircuitBreaker}
     */
    public ExecuteRSStringCircuitBreaker retry(int retryCount) {
        return new ExecuteRSStringCircuitBreaker(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain {@link ExecuteRSStringResponse}
     */
    public ExecuteRSStringResponse timeout(long timeout) {
        return new ExecuteRSStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries (on error).
     *
     * @param delay the amount of time in ms between an error and the retry
     * @return the createResponse chain {@link ExecuteRSStringResponse}
     */
    public ExecuteRSStringResponse delay(long delay) {
        return new ExecuteRSStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * put HTTP header to response
     *
     * @param key   the header name
     * @param value the header value
     * @return the response chain {@link ExecuteRSStringResponse}
     */
    public ExecuteRSStringResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headerMap,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

}
