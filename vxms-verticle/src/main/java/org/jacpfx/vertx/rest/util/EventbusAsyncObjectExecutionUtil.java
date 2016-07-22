package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObjectResponse;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncObjectExecutionUtil {

    public static ExecuteRSObjectResponse mapToObjectResponse(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx _vertx, Throwable _t,
                                                              Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                              ThrowableSupplier<Serializable> _objectSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                              Function<Throwable, Serializable> _errorHandlerObject, int _httpStatusCode, int _retryCount,long _timeout, long _delay) {
        final DeliveryOptions deliveryOptions = Optional.ofNullable(options).orElse(new DeliveryOptions());
        final ExecuteEventBusObjectCallAsync excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                         context, headers,
                                                                         encoder, errorHandler, errorHandlerObject,
                                                                         httpStatusCode, retryCount,timeout, delay) ->
                sendMessageAndSupplyObjectHandler(id, message, options, errorFunction, objectFunction, deliveryOptions, vertx, t,
                        errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount,timeout, delay);


        return new ExecuteRSObjectResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _objectSupplier, excecuteEventBusAndReply,
                _encoder, _errorHandler, _errorHandlerObject, _httpStatusCode, _retryCount,_timeout,_delay);
    }

    protected static void sendMessageAndSupplyObjectHandler(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                            ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, DeliveryOptions deliveryOptions,
                                                            Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                            Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount, long timeout, long delay) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createObjectSupplierAndExecute(id, message, options, errorFunction, objectFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerObject, httpStatusCode,
                                        retryCount,timeout,delay, event));
    }

    private static void createObjectSupplierAndExecute(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                       Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                                       int httpStatusCode, int retryCount,  long timeout, long delay,AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<Serializable> objectSupplier = createObjectSupplier(id, message, options, errorFunction, objectFunction, vertx, t,
                errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout,delay,event);


        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, null, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount).execute();
        } else if (event.failed() && retryCount > 0) {
            retryObjectOperation(id, message, options, errorFunction, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount, timeout, delay);
        }
    }

    private static void retryObjectOperation(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int httpStatusCode, int retryCount, long timeout, long delay) {
        mapToObjectResponse(id, message, options, errorFunction, objectFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount - 1,timeout,delay).
                execute();
    }

    private static ThrowableSupplier<Serializable> createObjectSupplier(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                                        ThrowableFunction<AsyncResult<Message<Object>>, Serializable> objectFunction, Vertx vertx, Throwable t,
                                                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                        Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject,
                                                                        int httpStatusCode, int retryCount,long timeout, long delay,AsyncResult<Message<Object>> event) {
        return () -> {
            Serializable resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryObjectOperation(id, message, options, errorFunction, objectFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerObject, httpStatusCode, retryCount,timeout,delay);
                } else {
                    resp = (Serializable) executeErrorFunction(event, errorFunction);
                }
            } else {
                resp = objectFunction.apply(event);
            }

            return resp;
        };
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
