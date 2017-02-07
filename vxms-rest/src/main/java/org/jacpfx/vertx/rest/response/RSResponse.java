package org.jacpfx.vertx.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByteResponse;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicObjectResponse;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class RSResponse {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final Map<String, String> headers;

    public RSResponse(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
    }

    /**
     * Switch to blocking mode
     *
     * @return @see{org.jacpfx.vertx.rest.response.RSAsyncResponse}
     */
    public RSResponseBlocking blocking() {
        return new RSResponseBlocking(methodId, vertx, t, errorMethodHandler, context, headers);
    }

    /**
     * Returns a byte array to the target type
     *
     * @param byteConsumer consumes a io.vertx.core.Future to compleate with a byte response
     * @return @see{org.jacpfx.vertx.rest.createResponse.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicByteResponse byteResponse(ThrowableFutureConsumer<byte[]> byteConsumer) {
        return new ExecuteRSBasicByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteConsumer, null, null, null, null, 0, 0, 0, 0l, 0l);
    }

    /**
     * Returns a String to the target type
     *
     * @param stringConsumer consumes a io.vertx.core.Future to compleate with a String response
     * @return @see{org.jacpfx.vertx.rest.createResponse.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicStringResponse stringResponse(ThrowableFutureConsumer<String> stringConsumer) {
        return new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, null, null, null, null, 0, 0, 0, 0l, 0l);
    }

    /**
     * Returns a Serializable to the target type
     *
     * @param objectConsumer consumes a io.vertx.core.Future to compleate with a Serialized Object response
     * @return @see{org.jacpfx.vertx.rest.createResponse.ExecuteRSBasicResponse}
     */
    public ExecuteRSBasicObjectResponse objectResponse(ThrowableFutureConsumer<Serializable> objectConsumer, Encoder encoder) {
        return new ExecuteRSBasicObjectResponse(methodId, vertx, t, errorMethodHandler, context, headers, objectConsumer, null, encoder, null, null, 0, 0, 0, 0l, 0l);
    }


    /**
     * Ends the createResponse. If no data has been written to the createResponse body,
     * the actual createResponse won't get written until this method gets called.
     * <p>
     * Once the createResponse has ended, it cannot be used any more.
     */
    public void end() {
        context.response().end();
    }

    /**
     * Ends the createResponse. If no data has been written to the createResponse body,
     * the actual createResponse won't get written until this method gets called.
     * <p>
     * Once the createResponse has ended, it cannot be used any more.
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
