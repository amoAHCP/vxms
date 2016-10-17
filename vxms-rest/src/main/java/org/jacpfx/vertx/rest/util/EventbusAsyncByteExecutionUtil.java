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

    public static ExecuteRSByteResponse mapToByteResponse(String _id, Object _message, DeliveryOptions _options,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, byte[]> _byteFunction, Vertx _vertx, Throwable _t,
                                                          Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                          ThrowableSupplier<byte[]> _byteSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                          Function<Throwable, byte[]> _errorHandlerByte, int _httpStatusCode, int _retryCount, long _timeout, long _delay) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusByteCallAsync excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                       context, headers,
                                                                       encoder, errorHandler, errorHandlerByte,
                                                                       httpStatusCode, retryCount, timeout, delay) ->
                sendMessageAndSupplyByteHandler(_id, _message, _options,
                        _byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers,
                        encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay);


        return new ExecuteRSByteResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _byteSupplier,
                excecuteEventBusAndReply, _encoder, _errorHandler, _errorHandlerByte, _httpStatusCode, _retryCount, _timeout, _delay);
    }

    private static void sendMessageAndSupplyByteHandler(String id, Object message, DeliveryOptions options,
                                                        ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions,
                                                        Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                        Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createByteSupplierAndExecute(id, message, options, byteFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerByte, httpStatusCode,
                                        retryCount, timeout, delay, event));
    }

    private static void createByteSupplierAndExecute(String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                     Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                     Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<byte[]> byteSupplier = createByteSupplier(id, message, options, byteFunction, vertx, t, errorMethodHandler,
                context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay, event);

        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSByteResponse(vertx, t, errorMethodHandler, context, headers, byteSupplier, null, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay).execute();
        } else if (event.failed() && retryCount > 0) {
            retryByteOperation(id, message, options, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay);
        }
    }

    private static void retryByteOperation(String id, Object message, DeliveryOptions options,
                                           ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler,
                                           RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                           Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay) {
        mapToByteResponse(id, message, options, byteFunction, vertx, t, errorMethodHandler,
                context, headers, null, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount - 1, timeout, delay).
                execute();
    }


    private static ThrowableSupplier<byte[]> createByteSupplier(String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                                Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                                Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay, AsyncResult<Message<Object>> event) {
        return () -> {
            byte[] resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryByteOperation(id, message, options, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, timeout, delay);
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
