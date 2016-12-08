package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncObjectExecutionUtil {

    public static ExecuteRSObjectResponse mapToObjectResponse(String _methodId,String id, Object message, DeliveryOptions options,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx _vertx, Throwable _t,
                                                              Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                              ThrowableSupplier<Serializable> _objectSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                              Function<Throwable, Serializable> _errorHandlerObject, int _httpStatusCode, int _retryCount, long _timeout, long _delay, long _circuitBreakerTimeout) {
        final DeliveryOptions deliveryOptions = Optional.ofNullable(options).orElse(new DeliveryOptions());
        final ExecuteEventBusObjectCallAsync excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                         context, headers,
                                                                         encoder, errorHandler, errorHandlerObject,
                                                                         httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout) ->
                sendMessageAndSupplyObjectHandler(_methodId,id, message, options, objectFunction, deliveryOptions, vertx, t,
                        errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);


        return new ExecuteRSObjectResponse(_methodId,_vertx, _t, _errorMethodHandler, _context, _headers, _objectSupplier, excecuteEventBusAndReply,
                _encoder, _errorHandler, _errorHandlerObject, _httpStatusCode, _retryCount, _timeout, _delay, _circuitBreakerTimeout);
    }

    private static void sendMessageAndSupplyObjectHandler(String methodId,String id, Object message, DeliveryOptions options,
                                                            ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, DeliveryOptions deliveryOptions,
                                                            Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                            Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createObjectSupplierAndExecute(methodId,id, message, options, objectFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerObject, httpStatusCode,
                                        retryCount, timeout, delay, circuitBreakerTimeout, event));
    }

    private static void createObjectSupplierAndExecute(String methodId,String id, Object message, DeliveryOptions options,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                                       int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<Serializable> objectSupplier = createObjectSupplier(methodId,id, message, options,  objectFunction, vertx, t,
                errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay,circuitBreakerTimeout, event);


        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSObjectResponse(methodId,vertx, t, errorMethodHandler, context, headers, objectSupplier, null,
                    encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryObjectOperation(methodId,id, message, options, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
        }
    }

    private static void retryObjectOperation(String methodId,String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                             Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                             int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        mapToObjectResponse(methodId,id, message, options, objectFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount - 1, timeout, delay, circuitBreakerTimeout).
                execute();
    }

    private static ThrowableSupplier<Serializable> createObjectSupplier(String methodId,String id, Object message, DeliveryOptions options,
                                                                        ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                        Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                                                        int httpStatusCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout, AsyncResult<Message<Object>> event) {
        return () -> {
            Serializable resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryObjectOperation(methodId,id, message, options,  objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay, circuitBreakerTimeout);
                } else {
                    throw event.cause();
                }
            } else {
                resp = objectFunction.apply(event);
            }

            return resp;
        };
    }


}
