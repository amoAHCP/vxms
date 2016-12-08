package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByteResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusByteExecutionUtil {

    public static ExecuteRSBasicByteResponse mapToByteResponse(String _methodId, String _id, Object _message, DeliveryOptions _options,
                                                               ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> _byteFunction, Vertx _vertx, Throwable _t,
                                                               Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                               ThrowableFutureConsumer<byte[]> _byteSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                               ThrowableErrorConsumer<Throwable, byte[]> _onFailureRespond, int _httpStatusCode, int _retryCount, long _timeout, long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusByteCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                  context, headers,
                                                                  encoder, errorHandler, onFailureRespond,
                                                                  httpStatusCode, retryCount, timeout, circuitBreakerTimeout) ->
                sendMessageAndSupplyByteHandler(_methodId, _id, _message, _options, _byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);


        return new ExecuteRSBasicByteResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _byteSupplier, excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _retryCount, _timeout, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyByteHandler(String methodId, String id, Object message, DeliveryOptions options,
                                                        ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                        Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                        int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createByteSupplierAndExecute(methodId, id, message, options, byteFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode,
                                        retryCount, timeout, circuitBreakerTimeout, event));
    }

    private static void createByteSupplierAndExecute(String methodId, String id, Object message, DeliveryOptions options,
                                                     ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                     Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                     Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                     int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<byte[]> byteSupplier = createByteSupplier(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler, context,
                headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, event);

        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryByteOperation(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
        }
    }

    private static void retryByteOperation(String methodId, String id, Object message, DeliveryOptions options,
                                           ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                           Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                           Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                           int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        mapToByteResponse(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler,
                context, headers, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount - 1, timeout, circuitBreakerTimeout).
                execute();
    }


    private static ThrowableFutureConsumer<byte[]> createByteSupplier(String methodId, String id, Object message, DeliveryOptions options,
                                                                      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                                      Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                      Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                                      int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        return (future) -> {
            byte[] resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryByteOperation(methodId, id, message, options, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
                } else {
                    // handle default error chain
                    throw event.cause();
                }
            } else {
                byteFunction.accept(event, future);
            }
        };
    }


}
