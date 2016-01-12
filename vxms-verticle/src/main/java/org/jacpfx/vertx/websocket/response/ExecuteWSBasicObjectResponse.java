package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the response methods and executes the response chain.
 */
public class ExecuteWSBasicObjectResponse {
    protected final WebSocketEndpoint[] endpoint;
    protected final Vertx vertx;
    protected final CommType commType;
    protected final ThrowableSupplier<Serializable> objectSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final Function<Throwable, Serializable> errorHandlerObject;
    protected final WebSocketRegistry registry;
    protected final int retryCount;


    protected ExecuteWSBasicObjectResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, Serializable> errorHandlerObject, WebSocketRegistry registry, int retryCount) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.commType = commType;
        this.objectSupplier = objectSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorMethodHandler = errorMethodHandler;
        this.errorHandlerObject = errorHandlerObject;
        this.registry = registry;
        this.retryCount = retryCount;
    }

    /**
     * defines an action if an error occurs, this is an intermediate method which will not result in an output value
     *
     * @param errorHandler the handler to execute on error
     * @return the response chain
     */
    public ExecuteWSBasicObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSBasicObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerObject the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteWSBasicObjectResponse onObjectResponseError(Function<Throwable, Serializable> errorHandlerObject) {
        return new ExecuteWSBasicObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount);
    }

    /**
     * Defines how many times a retry will be done for the response method
     *
     * @param retryCount
     * @return the response chain
     */
    public ExecuteWSBasicObjectResponse retry(int retryCount) {
        return new ExecuteWSBasicObjectResponse(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount);
    }

    /**
     * Executes the response chain
     */
    public void execute() {
        int retry = retryCount > 0 ? retryCount : 0;
        Optional.ofNullable(objectSupplier).
                ifPresent(supplier ->
                        Optional.ofNullable(WebSocketExecutionUtil.executeRetryAndCatch(supplier,
                                null,
                                errorHandler,
                                errorHandlerObject,
                                errorMethodHandler,
                                retry)
                        ).
                                ifPresent(value -> WebSocketExecutionUtil.encode(value, encoder).
                                        ifPresent(val -> WebSocketExecutionUtil.sendObjectResult(val, commType, vertx, registry, endpoint))));
    }


}