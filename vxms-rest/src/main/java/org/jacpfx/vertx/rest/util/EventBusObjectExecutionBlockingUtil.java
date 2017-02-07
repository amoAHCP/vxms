package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.eventbus.blocking.EventBusBlockingExecution;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventBusObjectCallBlocking;
import org.jacpfx.vertx.rest.interfaces.blocking.RecursiveBlockingExecutor;
import org.jacpfx.vertx.rest.interfaces.blocking.RetryBlockingExecutor;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventBusObjectExecutionBlockingUtil {

    public static ExecuteRSObjectResponse mapToObjectResponse(String _methodId,
                                                              String _targetId,
                                                              Object _message,
                                                              DeliveryOptions _options,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, Serializable> _objectFunction,
                                                              Vertx _vertx, Throwable _t,
                                                              Consumer<Throwable> _errorMethodHandler,
                                                              RoutingContext _context, Map<String, String> _headers,
                                                              ThrowableSupplier<Serializable> _objectSupplier,
                                                              Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                              ThrowableFunction<Throwable, Serializable> _onFailureRespond,
                                                              int _httpStatusCode, int _httpErrorCode,
                                                              int _retryCount, long _timeout,
                                                              long _delay, long _circuitBreakerTimeout) {
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
            mapToObjectResponse(methodId,
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
                new ExecuteRSObjectResponse(methodId,
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

        final ExecuteEventBusObjectCallBlocking excecuteEventBusAndReply = (vertx, t,
                                                                            errorMethodHandler,
                                                                            context, headers,
                                                                            encoder, errorHandler,
                                                                            onFailureRespond,
                                                                            httpStatusCode, httpErrorCode,
                                                                            retryCount, timeout,
                                                                            delay, circuitBreakerTimeout) ->
                EventBusBlockingExecution.sendMessageAndSupplyHandler(_methodId,
                        _targetId, _message,
                        _objectFunction,
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
        return new ExecuteRSObjectResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _objectSupplier, excecuteEventBusAndReply,
                _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }


}
