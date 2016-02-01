package org.jacpfx.vertx.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionHandler;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSString extends ExecuteRSBasicString {
    protected final long delay;
    protected final long timeout;

    public ExecuteRSString(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<String> stringSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, async, stringSupplier, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        final ExecuteRSString lastStep = new ExecuteRSString(vertx, t, errorMethodHandler, context, headers, async, stringSupplier, encoder, errorHandler, errorHandlerString, status.code(), retryCount, delay, timeout);
        lastStep.execute();
    }

    @Override
    public void execute() {
        Optional.ofNullable(stringSupplier).
                ifPresent(supplier ->
                        this.vertx.executeBlocking(handler ->
                                        RESTExecutionHandler.executeRetryAndCatchAsync(context.response(), supplier, handler, errorHandler, errorHandlerString, errorMethodHandler, vertx, retryCount, timeout, delay),
                                false,
                                (Handler<AsyncResult<String>>) value -> {
                                    if (value.failed()) return;
                                    repond(value.result());
                                })
                );


    }


    protected void updateResponseHaders() {
        Optional.ofNullable(headers).ifPresent(h -> h.entrySet().stream().forEach(entry -> context.response().putHeader(entry.getKey(), entry.getValue())));
    }

}
