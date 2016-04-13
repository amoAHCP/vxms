package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCallAsync;
import org.jacpfx.vertx.rest.response.async.ExecuteRSStringResponse;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusAsyncResponse {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String id;
    private final Object message;
    private final DeliveryOptions options;
    private final Function<AsyncResult<Message<Object>>, ?> errorFunction;


    public EventBusAsyncResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.id = id;
        this.message = message;
        this.options = options;
        this.errorFunction = errorFunction;
    }


    public ExecuteRSStringResponse mapToStringResponse(ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction) {
        return mapToStringResponse(id, message,options,errorFunction,stringFunction,vertx, t, errorMethodHandler, context, null, null,  null, null, null, 0, 0, 0, 0);
    }


    public ExecuteRSStringResponse mapToStringResponse(String _id, Object _message, DeliveryOptions _options, Function<AsyncResult<Message<Object>>, ?> _errorFunction,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, String> _stringFunction, Vertx _vertx, Throwable _t, Consumer<Throwable> _errorMethodHandler,
                                                       RoutingContext _context, Map<String, String> _headers, ThrowableSupplier<String> _stringSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                       Function<Throwable, String> _errorHandlerString, int _httpStatusCode, int _retryCount, long _timeout, long _delay) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                         context, headers,
                                                                         encoder, errorHandler, errorHandlerString,
                                                                         httpStatusCode, retryCount, timeout, delay) ->
                sendMessageAndSupplyStringHandler(_id, _message, _options, _errorFunction, _stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, timeout, delay);

        return new ExecuteRSStringResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _stringSupplier,
                 excecuteAsyncEventBusAndReply,_encoder, _errorHandler, _errorHandlerString, _httpStatusCode, _retryCount, _timeout, _delay);
    }

    private  void sendMessageAndSupplyStringHandler(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                          Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                          Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount,long timeout, long delay) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(id, message, options, errorFunction, stringFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerString, httpStatusCode,
                                        retryCount, timeout,delay,event));
    }

    private  void createStringSupplierAndExecute(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                       Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, long timeout, long delay,AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<String> stringSupplier = createStringSupplier(id, message, options, errorFunction, stringFunction,
                vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, timeout, delay,event);
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier,null,
                    encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount,timeout,delay).execute();
        } else if (event.failed() && retryCount > 0) {
            mapToStringResponse(id, message, options, errorFunction, stringFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount - 1,timeout,delay).execute();
        }
    }


    private ThrowableSupplier<String> createStringSupplier(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                                  Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                                  Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount,long timeout, long delay, AsyncResult<Message<Object>> event) {
        return () -> {
            String resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringSupplier(id, message, options, errorFunction, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount,timeout,delay);
                } else {
                    resp = (String) executeErrorFunction(event, errorFunction);
                }
            } else {
                resp = stringFunction.apply(event);
            }

            return resp;
        };
    }

    private  void retryStringSupplier(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                            Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                            Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount,long timeout, long delay) {
        final int rcNew = retryCount - 1;
        mapToStringResponse(id, message, options, errorFunction, stringFunction, vertx, t, errorMethodHandler,
                context, headers, null,
                encoder, errorHandler, errorHandlerString,
                httpStatusCode, rcNew,timeout,delay).execute();
    }


    private static Object executeErrorFunction(AsyncResult<Message<Object>> event, Function<AsyncResult<Message<Object>>, ?> errorFunction) throws Throwable {
        Object resp;
        final Optional<? extends Function<AsyncResult<Message<Object>>, ?>> ef = Optional.ofNullable(errorFunction);
        if (!ef.isPresent()) throw event.cause();
        final Function<AsyncResult<Message<Object>>, ?> localErrorFunction = ef.get();
        resp = localErrorFunction.apply(event);
        return resp;
    }

    protected <T, R> void sendMessage(Function<AsyncResult<Message<T>>, R> stringFunction, CompletableFuture<R> cf,
                                      Vertx vertx, DeliveryOptions options, Function<AsyncResult<Message<T>>, ?> errorFunction,
                                      String id, Object message) {
        vertx.eventBus().send(id, message, options, (Handler<AsyncResult<Message<T>>>) event -> {
            if (event.failed()) {
                final Optional<? extends Function<AsyncResult<Message<T>>, ?>> ef = Optional.ofNullable(errorFunction);
                if (!ef.isPresent()) cf.obtrudeException(event.cause());
                ef.ifPresent(function -> {
                    try {
                        final R resp = (R) function.apply(event);
                        cf.complete(resp);
                    } catch (Exception e) {
                        cf.obtrudeException(e);
                    }
                });

            } else {

                try {
                    R resp = stringFunction.apply(event);
                    cf.complete(resp);
                } catch (Exception e) {
                    cf.obtrudeException(e);
                }

            }
        });
    }


    public EventBusAsyncResponse deliveryOptions(DeliveryOptions options) {
        return new EventBusAsyncResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }

    public EventBusAsyncResponse onErrorResult(Function<AsyncResult<Message<Object>>, ?> errorFunction) {
        return new EventBusAsyncResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }


}
