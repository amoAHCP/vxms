package org.jacpfx.vertx.rest.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventbusByteCallBlocking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API for byte responses, defines access to failure handling, timeouts,...
 */
public class ExecuteRSByteResponse extends ExecuteRSByte {

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                         the method identifier
     * @param vertx                            the vertx instance
     * @param failure                          the failure thrown while task execution
     * @param errorMethodHandler               the error handler
     * @param context                          the vertx routing context
     * @param headers                          the headers to pass to the response
     * @param byteSupplier                     the supplier, producing the byte response
     * @param excecuteBlockingEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                          the encoder to encode your objects
     * @param errorHandler                     the error handler
     * @param onFailureRespond                 the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode                   the http status code to set for response
     * @param httpErrorCode                    the http error code to set in case of failure handling
     * @param retryCount                       the amount of retries before failure execution is triggered
     * @param timeout                          the amount of time before the execution will be aborted
     * @param delay                            the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout            the amount of time before the circuit breaker closed again
     */
    public ExecuteRSByteResponse(String methodId,
                                 Vertx vertx,
                                 Throwable failure,
                                 Consumer<Throwable> errorMethodHandler,
                                 RoutingContext context,
                                 Map<String, String> headers,
                                 ThrowableSupplier<byte[]> byteSupplier,
                                 ExecuteEventbusByteCallBlocking excecuteBlockingEventBusAndReply,
                                 Encoder encoder,
                                 Consumer<Throwable> errorHandler,
                                 ThrowableFunction<Throwable, byte[]> onFailureRespond,
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
                byteSupplier,
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
     * @return the createResponse chain {@link ExecuteRSByteOnFailureCode}
     */
    public ExecuteRSByteOnFailureCode onFailureRespond(ThrowableFunction<Throwable, byte[]> onFailureRespond) {
        return new ExecuteRSByteOnFailureCode(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                byteSupplier,
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
     * Will be executed on each error
     *
     * @param errorHandler the handler to execute on error event
     * @return the createResponse chain {@link ExecuteRSByteResponse}
     */
    public ExecuteRSByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSByteResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                byteSupplier,
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
     * @return the createResponse chain {@link ExecuteRSByteCircuitBreaker}
     */
    public ExecuteRSByteCircuitBreaker retry(int retryCount) {
        return new ExecuteRSByteCircuitBreaker(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                byteSupplier,
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
     * @return the createResponse chain {@link ExecuteRSByteResponse}
     */
    public ExecuteRSByteResponse timeout(long timeout) {
        return new ExecuteRSByteResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                byteSupplier,
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
     * Defines the delay (in ms) between the createResponse retries.
     *
     * @param delay the amount of delay in ms between an error and the retry attempt
     * @return the createResponse chain {@link ExecuteRSByteResponse}
     */
    public ExecuteRSByteResponse delay(long delay) {
        return new ExecuteRSByteResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                byteSupplier,
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
     * @return the response chain {@link ExecuteRSByteResponse}
     */
    public ExecuteRSByteResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSByteResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headerMap,
                byteSupplier,
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
