package org.jacpfx.vertx.rest.response;

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

    public RSResponse putHeader(String name, String value) {
        headers.put(name, value);
        return new RSResponse(vertx, t, errorMethodHandler, context, headers, async);
    }

    public RSResponse async() {
        return new RSResponse(vertx, t, errorMethodHandler, context, headers, true);
    }

    /**
     * Retunrs a byte array to the target type
     *
     * @param byteSupplier supplier which returns the response value as byte array
     * @return @see{org.jacpfx.vertx.rest.response.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicResponse byteResponse(ThrowableSupplier<byte[]> byteSupplier) {
        return new ExecuteRSBasicResponse(vertx,t,errorMethodHandler,context,headers,async,byteSupplier,null,null,null, null, null, null, null, 0);
    }

    /**
     * Retunrs a String to the target type
     *
     * @param stringSupplier supplier which returns the response value as String
     * @return @see{org.jacpfx.vertx.rest.response.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicResponse stringResponse(ThrowableSupplier<String> stringSupplier) {
        return new ExecuteRSBasicResponse(vertx,t,errorMethodHandler,context,headers,async,null,stringSupplier,null,null, null, null, null, null, 0);
    }

    /**
     * Retunrs a Serializable to the target type
     *
     * @param objectSupplier supplier which returns the response value as Serializable
     * @return @see{org.jacpfx.vertx.rest.response.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicResponse objectResponse(ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
        return new ExecuteRSBasicResponse(vertx,t,errorMethodHandler,context,headers,async,null,null,objectSupplier,encoder, null, null, null, null, 0);
    }
}
