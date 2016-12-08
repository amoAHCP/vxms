package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSByteResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncByteExecutionUtil {

    public static ExecuteRSByteResponse mapToByteResponse(String _methodId, String _id, Object _message, DeliveryOptions _options,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, byte[]> _byteFunction, Vertx _vertx, Throwable _t,
                                                          Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                          ThrowableSupplier<byte[]> _byteSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                          Function<Throwable, byte[]> _errorHandlerByte, int _httpStatusCode, int _retryCount, long _timeout, long _delay, long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusByteCallAsync excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                       context, headers,
                                                                       encoder, errorHandler, errorHandlerByte,
                                                                       httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout) ->
                sendMessageAndSupplyByteHandler(_methodId, _id, _message, _options,
                        _byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers,
                        encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);


        return new ExecuteRSByteResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _byteSupplier,
                excecuteEventBusAndReply, _encoder, _errorHandler, _errorHandlerByte, _httpStatusCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyByteHandler(String methodId, String id, Object message, DeliveryOptions options,
                                                        ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions,
                                                        Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                        Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode,
                                                        int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createByteSupplierAndExecute(methodId, id, message, options, byteFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerByte, httpStatusCode,
                                        retryCount, timeout, delay, circuitBreakerTimeout, event));
    }

    private static void createByteSupplierAndExecute(String methodId, String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                     Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                     Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<byte[]> byteSupplier = createByteSupplier(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler,
                context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout, event);

        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, null, encoder, errorHandler,
                    errorHandlerByte, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryByteOperation(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }
    }

    private static void retryByteOperation(String methodId, String id, Object message, DeliveryOptions options,
                                           ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler,
                                           RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                           Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        mapToByteResponse(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler,
                context, headers, null, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount - 1, timeout, delay, circuitBreakerTimeout).
                execute();
    }


    private static ThrowableSupplier<byte[]> createByteSupplier(String methodId, String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                                Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                                Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        return () -> {
            byte[] resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryByteOperation(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
                } else {
                    throw event.cause();
                }
            } else {
                resp = byteFunction.apply(event);
            }

            return resp;
        };
    }

}
