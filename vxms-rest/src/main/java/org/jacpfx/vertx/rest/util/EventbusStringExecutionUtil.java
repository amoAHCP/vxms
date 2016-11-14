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
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusStringExecutionUtil {


    public static ExecuteRSBasicStringResponse mapToStringResponse(String _methodId, String _id, Object _message, DeliveryOptions _options,
                                                                   ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> _stringFunction, Vertx _vertx, Throwable _t,
                                                                   Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                                   ThrowableFutureConsumer<String> _stringConsumer, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                                   ThrowableErrorConsumer<Throwable, String> _onFailureRespond, int _httpStatusCode, int _retryCount, long _timeout, long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusStringCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                    context, headers,
                                                                    encoder, errorHandler, onFailureRespond,
                                                                    httpStatusCode, retryCount, timeout, circuitBreakerTimeout) ->
                sendMessageAndSupplyStringHandler(_methodId, _id, _message,  _stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);

        return new ExecuteRSBasicStringResponse(_methodId, _vertx, _t, _errorMethodHandler, _context, _headers, _stringConsumer, excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _retryCount, _timeout, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyStringHandler(String methodId, String id, Object message,
                                                          ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction,
                                                          DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                          Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                          ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(methodId, id, message, deliveryOptions, stringFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode,
                                        retryCount, timeout, circuitBreakerTimeout, event));
    }

    private static void createStringSupplierAndExecute(String methodId, String id, Object message, DeliveryOptions options,
                                                       ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                                       int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<String> stringSupplier = createStringSupplier(methodId, id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout, event);
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers,
                    stringSupplier, null, encoder, errorHandler, onFailureRespond,
                    httpStatusCode, retryCount, timeout, circuitBreakerTimeout).
                    execute();
        } else if (event.failed() && retryCount > 0) {
            // retry operation
            retryStringOperation(methodId, id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
        }
    }

    private static void retryStringOperation(String methodId, String id, Object message, DeliveryOptions options,
                                             ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                             Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                             Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                             int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        mapToStringResponse(methodId, id, message, options, stringFunction, vertx, t,
                errorMethodHandler, context, headers, null, encoder, errorHandler,
                onFailureRespond, httpStatusCode, retryCount - 1, timeout, circuitBreakerTimeout).
                execute();
    }


    private static ThrowableFutureConsumer<String> createStringSupplier(String methodId, String id, Object message, DeliveryOptions options,
                                                                        ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                        Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                                                        int httpStatusCode, int retryCount, long timeout, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        return (future) -> {
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringOperation(methodId, id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, circuitBreakerTimeout);
                } else {
                    // handle default error chain
                    throw event.cause();
                }
            } else {
                stringFunction.accept(event, future);
            }
        };
    }


}
