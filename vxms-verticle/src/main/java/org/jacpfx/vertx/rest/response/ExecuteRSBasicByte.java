package org.jacpfx.vertx.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionHandler;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByte {
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final boolean async;
    protected final ThrowableSupplier<byte[]> byteSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Function<Throwable, byte[]> errorHandlerByte;
    protected final int httpStatusCode;
    protected final int retryCount;

    public ExecuteRSBasicByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int httpStatusCode, int retryCount) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.async = async;
        this.byteSupplier = byteSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorHandlerByte = errorHandlerByte;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
    }

    public void execute(HttpResponseStatus status) {
        final ExecuteRSBasicByte lastStep = new ExecuteRSBasicByte(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, status.code(), retryCount);
        lastStep.execute();
    }


    public void execute() {
        Optional.ofNullable(byteSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            byte[] result = new byte[0];
                            boolean errorHandling = false;
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();
                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionHandler.handleError( result, errorHandler, errorHandlerByte, errorMethodHandler, e);
                                        errorHandling = true;
                                    } else {
                                        RESTExecutionHandler.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (errorHandling && result == null) return;
                            repond(result);
                        }
                );


    }

    protected void repond(byte[] result) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            RESTExecutionHandler.updateResponseHaders(headers, response);
            RESTExecutionHandler.updateResponseStatusCode(httpStatusCode, response);
            if (result != null) {
                response.end(Buffer.buffer(result));
            } else {
                response.end();
            }
        }
    }


}
