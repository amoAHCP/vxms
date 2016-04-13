package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusStringExecutionUtil {






    public static ExecuteRSBasicStringResponse mapToStringResponse(String _id, Object _message, DeliveryOptions _options, Function<AsyncResult<Message<Object>>, ?> _errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> _stringFunction,
                                                                   Vertx _vertx, Throwable _t, Consumer<Throwable> _errorMethodHandler,
                                                                   RoutingContext _context, Map<String, String> _headers, ThrowableSupplier<String> _stringSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                                   Function<Throwable, String> _errorHandlerString, int _httpStatusCode, int _retryCount) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusStringCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                    context, headers,
                                                                    encoder, errorHandler, errorHandlerString,
                                                                    httpStatusCode, retryCount) ->
                sendMessageAndSupplyStringHandler(_id, _message, _options, _errorFunction, _stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);

        return new ExecuteRSBasicStringResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _stringSupplier, excecuteEventBusAndReply, _encoder, _errorHandler, _errorHandlerString, _httpStatusCode, _retryCount);
    }

    private static void sendMessageAndSupplyStringHandler(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(id, message, options, errorFunction, stringFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerString, httpStatusCode,
                                        retryCount, event));
    }

    private static void createStringSupplierAndExecute(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                       Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<String> stringSupplier = createStringSupplier(id, message, options, errorFunction, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, event);
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headers,
                    stringSupplier, null, encoder, errorHandler, errorHandlerString,
                    httpStatusCode, retryCount).
                    execute();
        } else if (event.failed() && retryCount > 0) {
            // retry operation
            retryStringOperation(id, message, options, errorFunction, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
        }
    }

    private static void retryStringOperation(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        mapToStringResponse(id, message, options, errorFunction, stringFunction, vertx, t,
                errorMethodHandler, context, headers, null, encoder, errorHandler,
                errorHandlerString, httpStatusCode, retryCount - 1).
                execute();
    }


    private static ThrowableSupplier<String> createStringSupplier(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                                  Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                                  Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, AsyncResult<Message<Object>> event) {
        return () -> {
            String resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringSupplier(id, message, options, errorFunction, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
                } else {
                    resp = (String) executeErrorFunction(event, errorFunction);
                }
            } else {
                resp = stringFunction.apply(event);
            }

            return resp;
        };
    }


    private static void retryStringSupplier(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                            Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                            Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        final int rcNew = retryCount - 1;
        mapToStringResponse(id, message, options, errorFunction, stringFunction, vertx, t, errorMethodHandler,
                context, headers, null,
                encoder, errorHandler, errorHandlerString,
                httpStatusCode, rcNew).execute();
    }


    private static Object executeErrorFunction(AsyncResult<Message<Object>> event, Function<AsyncResult<Message<Object>>, ?> errorFunction) throws Throwable {
        Object resp;
        final Optional<? extends Function<AsyncResult<Message<Object>>, ?>> ef = Optional.ofNullable(errorFunction);
        if (!ef.isPresent()) throw event.cause();
        final Function<AsyncResult<Message<Object>>, ?> localErrorFunction = ef.get();
        resp = localErrorFunction.apply(event);
        return resp;
    }
}
