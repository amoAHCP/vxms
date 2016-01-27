package org.jacpfx.vertx.rest.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionHandler;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSByte extends ExecuteRSBasicByte {
    protected final long delay;
    protected final long timeout;

    public ExecuteRSByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int retryCount,long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }

    @Override
    public void execute() {

        Optional.ofNullable(byteSupplier).
                ifPresent(supplier ->
                        this.vertx.executeBlocking(handler ->
                                        RESTExecutionHandler.executeRetryAndCatchAsync(context.response(), supplier, handler, errorHandler, errorHandlerByte, errorMethodHandler, vertx, retryCount, timeout, delay),
                                false,
                                (Handler<AsyncResult<byte[]>>) value -> {
                                    if (!context.response().ended()) {
                                        updateResponseHaders();
                                        if (value.result() != null) {
                                            context.response().end(Buffer.buffer(value.result()));
                                        } else {
                                            context.response().end();
                                        }
                                    }
                                })
                );


    }


}
