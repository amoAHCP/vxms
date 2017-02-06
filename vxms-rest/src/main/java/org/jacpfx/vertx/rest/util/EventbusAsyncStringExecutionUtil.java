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
import org.jacpfx.common.*;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncStringExecutionUtil {


    public static ExecuteRSStringResponse mapToStringResponse(String _methodId,
                                                              String _targetId,
                                                              Object _message,
                                                              DeliveryOptions _options,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, String> _stringFunction,
                                                              Vertx _vertx, Throwable _t,
                                                              Consumer<Throwable> _errorMethodHandler,
                                                              RoutingContext _context, Map<String, String> _headers,
                                                              ThrowableSupplier<String> _stringSupplier, Encoder _encoder,
                                                              Consumer<Throwable> _errorHandler,
                                                              ThrowableFunction<Throwable, String> _onFailureRespond,
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


        final ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply = (vertx, t,
                                                                              errorMethodHandler,
                                                                              context, headers,
                                                                              encoder, errorHandler,
                                                                              onFailureRespond,
                                                                              httpStatusCode, httpErrorCode,
                                                                              retryCount, timeout,
                                                                              delay, circuitBreakerTimeout) ->
                EventbusBlockingExecutionUtil.sendMessageAndSupplyHandler(_methodId,
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
        return new ExecuteRSStringResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _stringSupplier, excecuteAsyncEventBusAndReply,
                _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }


}
