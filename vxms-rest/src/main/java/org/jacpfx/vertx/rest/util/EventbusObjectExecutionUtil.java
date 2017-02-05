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
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObjectResponse;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusObjectExecutionUtil {

    public static ExecuteRSBasicObjectResponse mapToObjectResponse(String _methodId,
                                                                   String _targetId,
                                                                   Object _message,
                                                                   ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, Serializable> _objectFunction,
                                                                   DeliveryOptions _options,
                                                                   Vertx _vertx, Throwable _t,
                                                                   Consumer<Throwable> _errorMethodHandler,
                                                                   RoutingContext _context,
                                                                   Map<String, String> _headers,
                                                                   ThrowableFutureConsumer<Serializable> _objectConsumer,
                                                                   Encoder _encoder,
                                                                   Consumer<Throwable> _errorHandler,
                                                                   ThrowableErrorConsumer<Throwable, Serializable> _onFailureRespond,
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
                                     encoder,
                                     errorHandler,
                                     onFailureRespond,
                                     httpStatusCode,
                                     httpErrorCode, retryCount,
                                     timeout, circuitBreakerTimeout) -> {
            final int decrementedCount = retryCount - 1;
            mapToObjectResponse(methodId,
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
                new ExecuteRSBasicObjectResponse(methodId,
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

        final ExecuteEventBusObjectCall excecuteEventBusAndReply = (vertx, t,
                                                                    errorMethodHandler,
                                                                    context, headers,
                                                                    encoder, errorHandler,
                                                                    onFailureRespond, httpStatusCode,
                                                                    httpErrorCode, retryCount,
                                                                    timeout, circuitBreakerTimeout) ->
                EventbusExecutionUtil.sendMessageAndSupplyStringHandler(_methodId,
                        _targetId,
                        _message,
                        _objectFunction,
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

        return new ExecuteRSBasicObjectResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _objectConsumer, excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _circuitBreakerTimeout);
    }


}
