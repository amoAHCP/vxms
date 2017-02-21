package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.eventbus.blocking.EventbusBlockingExecution;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventbusStringCallBlocking;
import org.jacpfx.vertx.rest.interfaces.blocking.RecursiveBlockingExecutor;
import org.jacpfx.vertx.rest.interfaces.blocking.RetryBlockingExecutor;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Typed execution of event-bus calls and blocking string response
 */
public class EventbusStringExecutionBlockingUtil {

    /**
     * create execution chain for event-bus request and reply to rest
     *
     * @param _methodId              the method identifier
     * @param _targetId              the event-bus target id
     * @param _message               the message to send
     * @param _stringFunction        the function to process the result message
     * @param _options               the event-bus delivery options
     * @param _vertx                 the vertx instance
     * @param _failure               the failure thrown while task execution
     * @param _errorMethodHandler    the error-method handler
     * @param _context               the vertx routing context
     * @param _headers               the headers to pass to the response
     * @param _stringSupplier        the supplier, producing the string response
     * @param _encoder               the encoder to encode your objects
     * @param _errorHandler          the error handler
     * @param _onFailureRespond      the consumer that takes a Future with the alternate response value in case of failure
     * @param _httpStatusCode        the http status code to set for response
     * @param _httpErrorCode         the http error code to set in case of failure handling
     * @param _retryCount            the amount of retries before failure execution is triggered
     * @param _timeout               the amount of time before the execution will be aborted
     * @param _delay                 the delay between an error and the retry
     * @param _circuitBreakerTimeout the amount of time before the circuit breaker closed again
     * @return the execution chain {@link ExecuteRSStringResponse}
     */
    public static ExecuteRSStringResponse mapToStringResponse(String _methodId,
                                                              String _targetId,
                                                              Object _message,
                                                              DeliveryOptions _options,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, String> _stringFunction,
                                                              Vertx _vertx,
                                                              Throwable _failure,
                                                              Consumer<Throwable> _errorMethodHandler,
                                                              RoutingContext _context, Map<String, String> _headers,
                                                              ThrowableSupplier<String> _stringSupplier,
                                                              Encoder _encoder,
                                                              Consumer<Throwable> _errorHandler,
                                                              ThrowableFunction<Throwable, String> _onFailureRespond,
                                                              int _httpStatusCode,
                                                              int _httpErrorCode,
                                                              int _retryCount,
                                                              long _timeout,
                                                              long _delay,
                                                              long _circuitBreakerTimeout) {

        final DeliveryOptions _deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());


        final RetryBlockingExecutor retry = (methodId,
                                             targetId,
                                             message,
                                             byteFunction,
                                             deliveryOptions,
                                             vertx, t,
                                             errorMethodHandler,
                                             context,
                                             headers,
                                             encoder,
                                             errorHandler,
                                             onFailureRespond,
                                             httpStatusCode,
                                             httpErrorCode, retryCount,
                                             timeout, delay, circuitBreakerTimeout) -> {
            final int decrementedCount = retryCount - 1;
            mapToStringResponse(methodId,
                    targetId, message,
                    deliveryOptions,
                    byteFunction,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    null,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    decrementedCount,
                    timeout, delay,
                    circuitBreakerTimeout).
                    execute();
        };

        final RecursiveBlockingExecutor executor = (methodId,
                                                    vertx,
                                                    t,
                                                    errorMethodHandler,
                                                    context,
                                                    headers,
                                                    supplier,
                                                    encoder,
                                                    errorHandler,
                                                    onFailureRespond,
                                                    httpStatusCode, httpErrorCode,
                                                    retryCount, timeout, delay, circuitBreakerTimeout) ->
                new ExecuteRSStringResponse(methodId,
                        vertx, t,
                        errorMethodHandler,
                        context, headers,
                        supplier,
                        null,
                        encoder, errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount, timeout, delay,
                        circuitBreakerTimeout).
                        execute();


        final ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply = (vertx, t,
                                                                                 errorMethodHandler,
                                                                                 context, headers,
                                                                                 encoder, errorHandler,
                                                                                 onFailureRespond,
                                                                                 httpStatusCode, httpErrorCode,
                                                                                 retryCount, timeout,
                                                                                 delay, circuitBreakerTimeout) ->
                EventbusBlockingExecution.sendMessageAndSupplyHandler(_methodId,
                        _targetId, _message,
                        _stringFunction,
                        _deliveryOptions,
                        vertx, t,
                        errorMethodHandler,
                        context, headers,
                        encoder, errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount,
                        timeout, delay,
                        circuitBreakerTimeout,
                        executor, retry);
        return new ExecuteRSStringResponse(_methodId, _vertx, _failure, _errorMethodHandler, _context, _headers, _stringSupplier, excecuteAsyncEventBusAndReply,
                _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }


}
