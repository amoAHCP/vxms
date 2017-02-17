package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSByteResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSObjectResponse;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSStringResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API to define a Task and to reply the request with the output of your task.
 */
public class RSResponseBlocking {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final Map<String, String> headers;

    /**
     * Pass all needed values to execute the chain
     *
     * @param methodId           the method identifier
     * @param vertx              the vertx instance
     * @param failure            the failure thrown while task execution
     * @param errorMethodHandler the error handler
     * @param context            the vertx routing context
     * @param headers            the headers to pass to the response
     */
    public RSResponseBlocking(String methodId,
                              Vertx vertx,
                              Throwable failure,
                              Consumer<Throwable> errorMethodHandler,
                              RoutingContext context,
                              Map<String, String> headers) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
    }


    /**
     * Retunrs a byte array to the target type
     *
     * @param byteSupplier supplier which returns the createResponse value as byte array
     * @return {@link ExecuteRSByteResponse}
     */
    public ExecuteRSByteResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteRSByteResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                byteSupplier,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                0l,
                0l,
                0l);
    }

    /**
     * Retunrs a String to the target type
     *
     * @param stringSupplier supplier which returns the createResponse value as String
     * @return {@link ExecuteRSStringResponse}
     */
    public ExecuteRSStringResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteRSStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                0l,
                0l,
                0l);
    }

    /**
     * Retunrs a Serializable to the target type
     *
     * @param objectSupplier supplier which returns the createResponse value as Serializable
     * @param encoder        the encoder to serialize the object response
     * @return {@link ExecuteRSObjectResponse}
     */
    public ExecuteRSObjectResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteRSObjectResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                objectSupplier,
                null,
                encoder,
                null,
                null,
                0,
                0,
                0,
                0l,
                0l,
                0l);
    }
}
