package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusStringExecutionUtil {


    public static ExecuteRSBasicStringResponse mapToStringResponse(String _methodId,
                                                                   String _targetId,
                                                                   Object _message,
                                                                   ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> _stringFunction,
                                                                   DeliveryOptions _options,
                                                                   Vertx _vertx, Throwable _t,
                                                                   Consumer<Throwable> _errorMethodHandler,
                                                                   RoutingContext _context) {
        return mapToStringResponse(_methodId, _targetId, _message, _stringFunction, _options, _vertx, _t, _errorMethodHandler, _context,null,null,null,null,null,0,0,0,0,0);
    }


    public static ExecuteRSBasicStringResponse mapToStringResponse(String _methodId,
                                                                   String _targetId,
                                                                   Object _message,
                                                                   ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> _stringFunction,
                                                                   DeliveryOptions _options,
                                                                   Vertx _vertx, Throwable _t,
                                                                   Consumer<Throwable> _errorMethodHandler,
                                                                   RoutingContext _context,
                                                                   Map<String, String> _headers,
                                                                   ThrowableFutureConsumer<String> _stringConsumer,
                                                                   Encoder _encoder,
                                                                   Consumer<Throwable> _errorHandler,
                                                                   ThrowableErrorConsumer<Throwable, String> _onFailureRespond,
                                                                   int _httpStatusCode, int _httpErrorCode,
                                                                   int _retryCount, long _timeout, long _circuitBreakerTimeout) {

        final DeliveryOptions _deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final RetryExecutor retry = (methodId,
                                     id,
                                     message,
                                     stringFunction,
                                     deliveryOptions,
                                     vertx, t,
                                     errorMethodHandler,
                                     context,
                                     headers,
                                     consumer,
                                     encoder,
                                     errorHandler,
                                     onFailureRespond,
                                     httpStatusCode,
                                     httpErrorCode, retryCount,
                                     timeout, circuitBreakerTimeout) -> {
            final int decrementedCount = retryCount - 1;
            mapToStringResponse(methodId,
                    id, message,
                    stringFunction,
                    deliveryOptions,
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
                    timeout,
                    circuitBreakerTimeout).
                    execute();
        };
        final RecursiveExecutor executor = (methodId,
                                            vertx,
                                            t,
                                            errorMethodHandler,
                                            context,
                                            headers,
                                            stringConsumer,
                                            excecuteEventBusAndReply,
                                            encoder,
                                            errorHandler,
                                            onFailureRespond,
                                            httpStatusCode, httpErrorCode,
                                            retryCount, timeout, circuitBreakerTimeout) ->
                new ExecuteRSBasicStringResponse(methodId,
                        vertx, t,
                        errorMethodHandler,
                        context, headers,
                        stringConsumer,
                        null,
                        encoder, errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount, timeout,
                        circuitBreakerTimeout).
                        execute();

        final ExecuteEventBusStringCall excecuteEventBusAndReply = (vertx, t,
                                                                    errorMethodHandler,
                                                                    context, headers,
                                                                    encoder, errorHandler,
                                                                    onFailureRespond,
                                                                    httpStatusCode, httpErrorCode,
                                                                    retryCount, timeout, circuitBreakerTimeout) ->
                EventbusExecutionUtil.sendMessageAndSupplyStringHandler(_methodId,
                        _targetId,
                        _message,
                        _stringFunction,
                        _deliveryOptions,
                        vertx,
                        t, errorMethodHandler,
                        context, headers,
                        encoder, errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout, executor,retry);

        return new ExecuteRSBasicStringResponse(_methodId,
                _vertx, _t,
                _errorMethodHandler,
                _context, _headers,
                _stringConsumer,
                excecuteEventBusAndReply,
                _encoder, _errorHandler,
                _onFailureRespond,
                _httpStatusCode,
                _httpErrorCode,
                _retryCount, _timeout,
                _circuitBreakerTimeout);
    }


}
