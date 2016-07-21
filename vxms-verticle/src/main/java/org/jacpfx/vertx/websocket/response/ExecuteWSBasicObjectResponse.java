package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 18.12.15.
 * This class defines several error methods for the createResponse methods and executes the createResponse chain.
 */
public class ExecuteWSBasicObjectResponse extends ExecuteWSBasicObject{



    protected ExecuteWSBasicObjectResponse(WebSocketEndpoint[] endpoint, Vertx vertx, CommType commType, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Consumer<Throwable> errorMethodHandler, Function<Throwable, Serializable> errorHandlerObject, WebSocketRegistry registry, int retryCount) {
        super(endpoint,vertx,commType,objectSupplier,encoder,errorHandler,errorMethodHandler,errorHandlerObject,registry,retryCount);
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
     * @param encoder, the Object Encoder {@see org.jacpfx.common.encoder.Encoder}
     * @return the response chain
     */
    public ExecuteWSBasicObject onErrorResponse(Function<Throwable, Serializable> errorHandlerObject, Encoder encoder) {
        return new ExecuteWSBasicObject(endpoint, vertx, commType, objectSupplier, encoder, errorHandler, errorMethodHandler, errorHandlerObject, registry, retryCount);
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


}