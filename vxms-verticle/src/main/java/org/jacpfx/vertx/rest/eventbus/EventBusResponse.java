package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusCall;
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


    public void executeAndRespond() {
        vertx.eventBus().send(id, message, options != null ? options : new DeliveryOptions(), event -> {
            if (event.failed()) {
                Optional.ofNullable(errorFunction).ifPresent(function -> {
                    final HttpServerResponse response = context.response();
                    final Object errorResult = function.apply(event);
                    respond(response, errorResult);
                });

            } else {
                final HttpServerResponse response = context.response();
                Object resp = event.result().body();
                respond(response, resp);
            }
        });
    }

    protected void respond(HttpServerResponse response, Object resp) {
        if (resp instanceof String) {
            response.end((String) resp);
        } else if (resp instanceof byte[]) {

            response.end(Buffer.buffer((byte[]) resp));
        } else {
            // WebSocketExecutionUtil.encode(result, encoder).ifPresent(value -> RESTExecutionUtil.sendObjectResult(value, context.response()));
        }
    }

    public ExecuteRSBasicStringResponse mapToStringResponse(Function<AsyncResult<Message<Object>>, String> stringFunction) {

       return mapToStringResponse(stringFunction, vertx, t, errorMethodHandler, context, null, null, null, null, null, 0, 0);
    }

    protected ExecuteRSBasicStringResponse mapToStringResponse(Function<AsyncResult<Message<Object>>, String> stringFunction,
                                                                   Vertx _vertx, Throwable _t, Consumer<Throwable> _errorMethodHandler,
                                                                   RoutingContext _context, Map<String, String> _headers, ThrowableSupplier<String> _stringSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                                   Function<Throwable, String> _errorHandlerString, int _httpStatusCode, int _retryCount) {

        final ExecuteEventBusCall excecuteEventBusAndReply1 = (vertx, t, errorMethodHandler,
                                                               context, headers, excecuteEventBusAndReply,
                                                               encoder, errorHandler, errorHandlerString,
                                                               httpStatusCode, retryCount) ->
                vertx.
                        eventBus().
                        send(id, message, options != null ? options : new DeliveryOptions(),
                                event ->
                                        createSupplierAndExecute(stringFunction,
                                                vertx, t, errorMethodHandler,
                                                context, headers, encoder,
                                                errorHandler, errorHandlerString, httpStatusCode,
                                                retryCount, event));

        return new ExecuteRSBasicStringResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _stringSupplier, excecuteEventBusAndReply1, _encoder, _errorHandler, _errorHandlerString, _httpStatusCode, _retryCount);
    }

    private void createSupplierAndExecute(Function<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<String> stringSupplier = () -> {
            String resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringSupplier(stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
                } else {
                    resp = executeErrorFunction(event);
                }
            } else {
                resp = stringFunction.apply(event);
            }

            return resp;
        };


        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier, null, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount).execute();
        } else if (event.failed() && retryCount > 0) {
            final int rcNew = retryCount - 1;
            ExecuteRSBasicStringResponse ex = mapToStringResponse(stringFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerString, httpStatusCode, rcNew);
            ex.execute();
        }
    }

    private String executeErrorFunction(AsyncResult<Message<Object>> event) throws Throwable {
        String resp;
        final Optional<? extends Function<AsyncResult<Message<Object>>, ?>> ef = Optional.ofNullable(errorFunction);
        if (!ef.isPresent()) throw event.cause();
        final Function<AsyncResult<Message<Object>>, ?> errorFunction = ef.get();
        resp = (String) errorFunction.apply(event);
        return resp;
    }

    private void retryStringSupplier(Function<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        final int rcNew = retryCount - 1;
        mapToStringResponse(stringFunction, vertx, t, errorMethodHandler,
                context, headers, null,
                encoder, errorHandler, errorHandlerString,
                httpStatusCode, rcNew).execute();
    }




    public EventBusResponse deliveryOptions(DeliveryOptions options) {
        return new EventBusResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }

    public EventBusResponse onErrorResult(Function<AsyncResult<Message<Object>>, ?> errorFunction) {
        return new EventBusResponse(vertx, t, errorMethodHandler, context, id, message, options, errorFunction);
    }


}
