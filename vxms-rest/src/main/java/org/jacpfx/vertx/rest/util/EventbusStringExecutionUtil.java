package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFunction;
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


    public static ExecuteRSBasicStringResponse mapToStringResponse(String _id, Object _message, DeliveryOptions _options,
                                                                   ThrowableFunction<AsyncResult<Message<Object>>, String> _stringFunction, Vertx _vertx, Throwable _t,
                                                                   Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                                   ThrowableFutureConsumer<String> _stringConsumer, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                                   ThrowableErrorConsumer<Throwable, String> _onFailureRespond, int _httpStatusCode, int _retryCount, long _timeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusStringCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                    context, headers,
                                                                    encoder, errorHandler, onFailureRespond,
                                                                    httpStatusCode, retryCount, timeout) ->
                sendMessageAndSupplyStringHandler(_id, _message, _options, _stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);

        return new ExecuteRSBasicStringResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _stringConsumer, excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _retryCount, _timeout);
    }

    private static void sendMessageAndSupplyStringHandler(String id, Object message, DeliveryOptions options,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                          Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                          ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount, long timeout) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(id, message, options, stringFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode,
                                        retryCount, timeout, event));
    }

    private static void createStringSupplierAndExecute(String id, Object message, DeliveryOptions options,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                                       int httpStatusCode, int retryCount, long timeout, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<String> stringSupplier = createStringSupplier(id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, event);
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headers,
                    stringSupplier, null, encoder, errorHandler, onFailureRespond,
                    httpStatusCode, retryCount, timeout).
                    execute();
        } else if (event.failed() && retryCount > 0) {
            // retry operation
            retryStringOperation(id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
        }
    }

    private static void retryStringOperation(String id, Object message, DeliveryOptions options,
                                             ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                             Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                             Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                             int httpStatusCode, int retryCount, long timeout) {
        mapToStringResponse(id, message, options, stringFunction, vertx, t,
                errorMethodHandler, context, headers, null, encoder, errorHandler,
                onFailureRespond, httpStatusCode, retryCount - 1, timeout).
                execute();
    }


    private static ThrowableFutureConsumer<String> createStringSupplier(String id, Object message, DeliveryOptions options,
                                                                        ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                        Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                                                        int httpStatusCode, int retryCount, long timeout, AsyncResult<Message<Object>> event) {
        return (future) -> {

            String resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringOperation(id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
                } else {
                    // handle default error chain
                    throw event.cause();
                }
            } else {
                resp = stringFunction.apply(event);
            }

            future.complete(resp);
        };
    }


}
