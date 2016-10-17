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
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObjectResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusObjectExecutionUtil {

    public static ExecuteRSBasicObjectResponse mapToObjectResponse(String id, Object message, DeliveryOptions options,
                                                                   ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx _vertx, Throwable _t,
                                                                   Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                                   ThrowableFutureConsumer<Serializable> _objectConsumer, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                                   ThrowableErrorConsumer<Throwable, Serializable> _onFailureRespond, int _httpStatusCode, int _retryCount, long _timeout) {
        final DeliveryOptions deliveryOptions = Optional.ofNullable(options).orElse(new DeliveryOptions());
        final ExecuteEventBusObjectCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                    context, headers,
                                                                    encoder, errorHandler, onFailureRespond,
                                                                    httpStatusCode, retryCount, timeout) ->
                sendMessageAndSupplyObjectHandler(id, message, options, objectFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);


        return new ExecuteRSBasicObjectResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _objectConsumer, excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _retryCount, _timeout);
    }

    protected static void sendMessageAndSupplyObjectHandler(String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction,
                                                            DeliveryOptions deliveryOptions, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                            Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createObjectSupplierAndExecute(id, message, options, objectFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode,
                                        retryCount, timeout, event));
    }

    private static void createObjectSupplierAndExecute(String id, Object message, DeliveryOptions options,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond,
                                                       int httpStatusCode, int retryCount, long timeout, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<Serializable> objectSupplier = createObjectSupplier(id, message, options, objectFunction, vertx, t,
                errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, event);


        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryObjectOperation(id, message, options, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
        }
    }

    private static void retryObjectOperation(String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction,
                                             Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                             Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout) {
        mapToObjectResponse(id, message, options, objectFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount - 1, timeout).
                execute();
    }

    private static ThrowableFutureConsumer<Serializable> createObjectSupplier(String id, Object message, DeliveryOptions options,
                                                                              ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                                              Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                              Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond,
                                                                              int httpStatusCode, int retryCount, long timeout, AsyncResult<Message<Object>> event) {
        return (future) -> {
            Serializable resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryObjectOperation(id, message, options, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
                } else {
                    // handle default error chain
                    throw event.cause();
                }
            } else {
                resp = objectFunction.apply(event);
            }

            future.complete(resp);
        };
    }


}
