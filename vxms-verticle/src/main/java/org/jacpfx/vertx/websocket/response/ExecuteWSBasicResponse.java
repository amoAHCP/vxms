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
@Deprecated
public class ExecuteWSBasicResponse {
    protected final WebSocketEndpoint[] endpoint;
    protected final Vertx vertx;
    protected final CommType commType;
    protected final ThrowableSupplier<byte[]> byteSupplier;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final ThrowableSupplier<Serializable> objectSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final Function<Throwable, byte[]> errorHandlerByte;
    protected final Function<Throwable, String> errorHandlerString;
    protected final Function<Throwable, Serializable> errorHandlerObject;
    protected final WebSocketRegistry registry;
    protected final int retryCount;


    protected ExecuteWSBasicResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<byte[]> byteSupplier, ThrowableSupplier<String> stringSupplier, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, byte[]> errorHandlerByte, Function<Throwable, String> errorHandlerString, Function<Throwable, Serializable> errorHandlerObject, WebSocketRegistry registry, int retryCount) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.commType = commType;
        this.byteSupplier = byteSupplier;
        this.stringSupplier = stringSupplier;
        this.objectSupplier = objectSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorMethodHandler = errorMethodHandler;
        this.errorHandlerByte = errorHandlerByte;
        this.errorHandlerString = errorHandlerString;
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
    public ExecuteWSBasicResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerByte the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteWSBasicResponse onByteResponseError(Function<Throwable, byte[]> errorHandlerByte) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerString the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteWSBasicResponse onStringResponseError(Function<Throwable, String> errorHandlerString) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerObject the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteWSBasicResponse onObjectResponseError(Function<Throwable, Serializable> errorHandlerObject) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount);
    }

    /**
     * Defines how many times a retry will be done for the response method
     *
     * @param retryCount
     * @return the response chain
     */
    public ExecuteWSBasicResponse retry(int retryCount) {
        return new ExecuteWSBasicResponse(endpoint, vertx, commType, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, registry, retryCount);
    }

    /**
     * Executes the response chain
     */
    public void execute() {
        int retry = retryCount > 0 ? retryCount : 0;
        Optional.ofNullable(byteSupplier).
                ifPresent(supplier ->
                        Optional.ofNullable(WebSocketExecutionUtil.executeRetryAndCatch(supplier,
                                null,
                                errorHandler,
                                errorHandlerByte,
                                errorMethodHandler,
                                retry)
                        ).
                                ifPresent(value -> WebSocketExecutionUtil.sendBinary(commType, vertx, registry, endpoint, value)));
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