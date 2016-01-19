package org.jacpfx.vertx.rest.response;

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
public class ExecuteRSBasicObject {

    protected final Vertx vertx;
    protected final Throwable t;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final boolean async;
    protected final ThrowableSupplier<Serializable> objectSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Function<Throwable, Serializable> errorHandlerObject;
    protected final int retryCount;

    public ExecuteRSBasicObject(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async,  ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int retryCount) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.async = async;
        this.objectSupplier = objectSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorHandlerObject = errorHandlerObject;
        this.retryCount = retryCount;
    }


    public void execute() {
        Optional.ofNullable(objectSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            Serializable result = "";
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();
                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionHandler.handleError(context.response(), result, errorHandler, errorHandlerObject, e);
                                    } else {
                                        RESTExecutionHandler.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) {
                                updateResponseHaders();
                                WebSocketExecutionUtil.encode(result, encoder).ifPresent(value -> RESTExecutionHandler.sendObjectResult(value, context.response()));
                            }

                        }
                );
    }

    protected void updateResponseHaders() {
        Optional.ofNullable(headers).ifPresent(h -> h.entrySet().stream().forEach(entry -> context.response().putHeader(entry.getKey(), entry.getValue())));
    }
}
