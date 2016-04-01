package org.jacpfx.vertx.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class RSResponse {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final Map<String, String> headers;
    private final boolean async;

    public RSResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.async = async;
    }


    public RSAsyncResponse async() {
        return new RSAsyncResponse(vertx, t, errorMethodHandler, context, headers, true);
    }

    /**
     * Returns a byte array to the target type
     *
     * @param byteSupplier supplier which returns the response value as byte array
     * @return @see{org.jacpfx.vertx.rest.response.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicByteResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, null, null, null, null, 0, 0);
    }

    /**
     * Returns a String to the target type
     *
     * @param stringSupplier supplier which returns the response value as String
     * @return @see{org.jacpfx.vertx.rest.response.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicStringResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteRSBasicStringResponse(vertx, t, errorMethodHandler, context, headers, stringSupplier, null, null, null, null, 0, 0);
    }

    /**
     * Returns a Serializable to the target type
     *
     * @param objectSupplier supplier which returns the response value as Serializable
     * @return @see{org.jacpfx.vertx.rest.response.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicObjectResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteRSBasicObjectResponse(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, null, null, 0, 0);
    }


    /**
     * Ends the response. If no data has been written to the response body,
     * the actual response won't get written until this method gets called.
     * <p>
     * Once the response has ended, it cannot be used any more.
     */
    public void end() {
        context.response().end();
    }

    /**
     * Ends the response. If no data has been written to the response body,
     * the actual response won't get written until this method gets called.
     * <p>
     * Once the response has ended, it cannot be used any more.
     *
     * @param status, the HTTP Status code
     */
    public void end(HttpResponseStatus status) {
        if (status != null) {
            context.response().setStatusCode(status.code()).end();
        } else {
            context.response().end();
        }

    }
}
