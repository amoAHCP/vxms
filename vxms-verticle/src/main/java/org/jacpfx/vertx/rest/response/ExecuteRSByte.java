package org.jacpfx.vertx.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSByte extends ExecuteRSBasicByte {
    protected final long timeout;
    protected final long delay;


    public ExecuteRSByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, httpStatusCode, retryCount);
        this.timeout = timeout;
        this.delay = delay;
    }

    @Override
    public void execute(HttpResponseStatus status) {
        final ExecuteRSByte lastStep = new ExecuteRSByte(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, status.code(), retryCount, timeout, delay);
        lastStep.execute();
    }


    @Override
    public void execute() {

        Optional.ofNullable(byteSupplier).
                ifPresent(supplier ->
                        this.vertx.executeBlocking(handler ->
                                        RESTExecutionUtil.executeRetryAndCatchAsync(supplier, handler, errorHandler, errorHandlerByte, errorMethodHandler, vertx, retryCount, timeout, delay),
                                false,
                                (Handler<AsyncResult<byte[]>>) value -> {
                                    if (value.failed()) return;
                                    repond(value.result());
                                })
                );


    }


}
