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
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCallAsync;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByteResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSByteResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncByteExecutionUtil {

    public static ExecuteRSByteResponse mapToByteResponse(String _methodId,
                                                          String _id,
                                                          Object _message,
                                                          DeliveryOptions _options,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, byte[]> _byteFunction,
                                                          Vertx _vertx, Throwable _t,
                                                          Consumer<Throwable> _errorMethodHandler,
                                                          RoutingContext _context,
                                                          Map<String, String> _headers,
                                                          ThrowableSupplier<byte[]> _byteSupplier,
                                                          Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                          ThrowableFunction<Throwable, byte[]> _onFailureRespond,
                                                          int _httpStatusCode, int _httpErrorCode,
                                                          int _retryCount,
                                                          long _timeout, long _delay,
                                                          long _circuitBreakerTimeout) {

        final DeliveryOptions _deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());


        final RetryBlockingExecutor retry = (methodId,
                                             id,
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
            mapToByteResponse(methodId,
                    id, message,
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
                new ExecuteRSByteResponse(methodId,
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


        final ExecuteEventBusByteCallAsync excecuteEventBusAndReply = (vertx, t,
                                                                       errorMethodHandler,
                                                                       context, headers,
                                                                       encoder, errorHandler,
                                                                       errorHandlerByte,
                                                                       httpStatusCode, httpErrorCode,
                                                                       retryCount, timeout,
                                                                       delay, circuitBreakerTimeout) ->
                EventbusBlockingExecutionUtil.sendMessageAndSupplyHandler(_methodId,
                        _id, _message,
                        _byteFunction,
                        _deliveryOptions,
                        vertx, t,
                        errorMethodHandler,
                        context, headers,
                        encoder, errorHandler,
                        errorHandlerByte,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount,
                        timeout, delay,
                        circuitBreakerTimeout,
                        executor, retry);


        return new ExecuteRSByteResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _byteSupplier,
                excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }


}
