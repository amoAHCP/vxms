package org.jacpfx.vertx.rest.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.response.ExecuteRSStringResponse;

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




    public ExecuteRSStringResponse mapToStringResponse(Function<AsyncResult<Message<Object>>, String> stringFunction) {
        final ThrowableSupplier<String> stringSupplier = () -> {
            final CompletableFuture<String> cf = new CompletableFuture<>();
            sendMessage(stringFunction, cf, vertx, options != null ? options : new DeliveryOptions(), errorFunction, id, message);
            return cf.get();
        };
        return new ExecuteRSStringResponse(vertx, t, errorMethodHandler, context, null, stringSupplier, null, null, null, 0, 0, 0, 0);
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
