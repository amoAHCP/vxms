package org.jacpfx.vertx.rest.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionHandler;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSObject extends ExecuteRSBasicObject{
    protected final long delay;
    protected final long timeout;


    public ExecuteRSObject(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
        this.delay = delay;
        this.timeout = timeout;
    }


    @Override
    public void execute() {
        // TODO implement async
        Optional.ofNullable(objectSupplier).
                ifPresent(supplier ->
                        this.vertx.executeBlocking(handler ->
                                        RESTExecutionHandler.executeRetryAndCatchAsync(context.response(), supplier, handler, errorHandler, errorHandlerObject, errorMethodHandler, vertx, retryCount, timeout, delay),
                                false,
                                (Handler<AsyncResult<Serializable>>) result -> {
                                    if (!context.response().ended()) {
                                        updateResponseHaders();
                                        if (result.result() != null) {
                                            WebSocketExecutionUtil.encode(result.result(), encoder).ifPresent(value -> RESTExecutionHandler.sendObjectResult(value, context.response()));
                                        } else {
                                            context.response().end();
                                        }
                                    }
                                })
                );

    }


}
