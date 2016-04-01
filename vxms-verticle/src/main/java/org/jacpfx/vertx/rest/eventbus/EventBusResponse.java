package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.response.ExecuteRSBasicByteResponse;
import org.jacpfx.vertx.rest.response.ExecuteRSBasicStringResponse;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventBusResponse {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String id;
    private final Object message;
    private final DeliveryOptions options;
    private final Function<AsyncResult<Message<Object>>, ?> errorFunction;


    public EventBusResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.id = id;
        this.message = message;
        this.options = options;
        this.errorFunction = errorFunction;
    }

    public ExecuteRSBasicByteResponse mapToByteResponse(Function<AsyncResult<Message<Object>>, byte[]> byteFunction) {

        return mapToByteResponse(byteFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0, 0);
    }

    public ExecuteRSBasicStringResponse mapToStringResponse(Function<AsyncResult<Message<Object>>, String> stringFunction) {

        return mapToStringResponse(stringFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0, 0);
    }


    protected ExecuteRSBasicByteResponse mapToByteResponse(Function<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                                           Vertx _vertx, Throwable _t, Consumer<Throwable> _errorMethodHandler,
                                                           RoutingContext _context, Map<String, String> _headers, ThrowableSupplier<byte[]> _byteSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                           Function<Throwable, byte[]> _errorHandlerByte, int _httpStatusCode, int _retryCount) {

        ExecuteEventBusByteCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                            context, headers, _excecuteEventBusAndReply,
                                                            encoder, errorHandler, errorHandlerByte,
                                                            httpStatusCode, retryCount) ->
                vertx.
                        eventBus().
                        send(id, message, options != null ? options : new DeliveryOptions(),
                                event ->
                                        createByteSupplierAndExecute(byteFunction,
                                                vertx, t, errorMethodHandler,
                                                context, headers, encoder,
                                                errorHandler, errorHandlerByte, httpStatusCode,
                                                retryCount, event));


        return new ExecuteRSBasicByteResponse(_vertx, _t, _errorMethodHandler, _context, _headers, false, _byteSupplier, excecuteEventBusAndReply, _encoder, _errorHandler, _errorHandlerByte, _httpStatusCode, _retryCount);
    }

    protected ExecuteRSBasicStringResponse mapToStringResponse(Function<AsyncResult<Message<Object>>, String> stringFunction,
                                                               Vertx _vertx, Throwable _t, Consumer<Throwable> _errorMethodHandler,
                                                               RoutingContext _context, Map<String, String> _headers, ThrowableSupplier<String> _stringSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                               Function<Throwable, String> _errorHandlerString, int _httpStatusCode, int _retryCount) {

        final ExecuteEventBusStringCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                    context, headers, _excecuteEventBusAndReply,
                                                                    encoder, errorHandler, errorHandlerString,
                                                                    httpStatusCode, retryCount) ->
                vertx.
                        eventBus().
                        send(id, message, options != null ? options : new DeliveryOptions(),
                                event ->
                                        createStringSupplierAndExecute(stringFunction,
                                                vertx, t, errorMethodHandler,
                                                context, headers, encoder,
                                                errorHandler, errorHandlerString, httpStatusCode,
                                                retryCount, event));

        return new ExecuteRSBasicStringResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _stringSupplier, excecuteEventBusAndReply, _encoder, _errorHandler, _errorHandlerString, _httpStatusCode, _retryCount);
    }

    private void createStringSupplierAndExecute(Function<AsyncResult<Message<Object>>, String> stringFunction,
                                                Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler,
                                                RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<String> stringSupplier = createStringSupplier(stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, event);


        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier, null, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount).execute();
        } else if (event.failed() && retryCount > 0) {
            mapToStringResponse(stringFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount - 1).execute();
        }
    }

    private void createByteSupplierAndExecute(Function<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                              Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler,
                                              RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                              Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<byte[]> byteSupplier = createByteSupplier(byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount, event);


        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, false, byteSupplier, null, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount).execute();
        } else if (event.failed() && retryCount > 0) {
            mapToByteResponse(byteFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount - 1).execute();
        }
    }

    private ThrowableSupplier<byte[]> createByteSupplier(Function<AsyncResult<Message<Object>>, byte[]> byteFunction,
                                                         Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler,
                                                         RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                         Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, AsyncResult<Message<Object>> event) {
        return () -> {
            byte[] resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryByteSupplier(byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
                } else {
                    resp = (byte[]) executeErrorFunction(event);
                }
            } else {
                resp = byteFunction.apply(event);
            }

            return resp;
        };
    }

    private ThrowableSupplier<String> createStringSupplier(Function<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                           Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                           Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, AsyncResult<Message<Object>> event) {
        return () -> {
            String resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringSupplier(stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
                } else {
                    resp = (String) executeErrorFunction(event);
                }
            } else {
                resp = stringFunction.apply(event);
            }

            return resp;
        };
    }

    private Object executeErrorFunction(AsyncResult<Message<Object>> event) throws Throwable {
        Object resp;
        final Optional<? extends Function<AsyncResult<Message<Object>>, ?>> ef = Optional.ofNullable(errorFunction);
        if (!ef.isPresent()) throw event.cause();
        final Function<AsyncResult<Message<Object>>, ?> errorFunction = ef.get();
        resp = errorFunction.apply(event);
        return resp;
    }

    private void retryStringSupplier(Function<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                     Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                     Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        final int rcNew = retryCount - 1;
        mapToStringResponse(stringFunction, vertx, t, errorMethodHandler,
                context, headers, null,
                encoder, errorHandler, errorHandlerString,
                httpStatusCode, rcNew).execute();
    }

    private void retryByteSupplier(Function<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                   Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                   Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount) {
        final int rcNew = retryCount - 1;
        mapToByteResponse(byteFunction, vertx, t, errorMethodHandler,
                context, headers, null,
                encoder, errorHandler, errorHandlerByte,
                httpStatusCode, rcNew).execute();
    }


    public EventBusResponse deliveryOptions(DeliveryOptions options) {
        return new EventBusResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }

    public EventBusResponse onErrorResult(Function<AsyncResult<Message<Object>>, ?> errorFunction) {
        return new EventBusResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }


}
