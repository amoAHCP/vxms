package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the createResponse chain.
 */
public class ExecuteWSBasicString {
    protected final WebSocketEndpoint[] endpoint;
    protected final Vertx vertx;
    protected final CommType commType;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final Function<Throwable, String> errorHandlerString;
    protected final WebSocketRegistry registry;
    protected final int retryCount;


    protected ExecuteWSBasicString(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<String> stringSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, String> errorHandlerString, WebSocketRegistry registry, int retryCount) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.commType = commType;
        this.stringSupplier = stringSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorMethodHandler = errorMethodHandler;
        this.errorHandlerString = errorHandlerString;
        this.registry = registry;
        this.retryCount = retryCount;
    }



    /**
     * Executes the response chain
     */
    public void execute() {
        int retry = retryCount > 0 ? retryCount : 0;

        Optional.ofNullable(stringSupplier).
                ifPresent(supplier ->
                        Optional.ofNullable(WebSocketExecutionUtil.executeRetryAndCatch(supplier,
                                null,
                                errorHandler,
                                errorHandlerString,
                                errorMethodHandler,
                                retry)
                        ).
                                ifPresent(value -> WebSocketExecutionUtil.sendText(commType, vertx, registry, endpoint, value)));


    }


}