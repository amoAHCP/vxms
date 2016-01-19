package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
    protected final int retryCount;

    public ExecuteRSBasicByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int retryCount) {
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
    }


    public void execute() {


        Optional.ofNullable(byteSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            byte[] result = new byte[0];
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();

                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionHandler.handleError(context.response(), result, errorHandler, errorHandlerByte, e);
                                    } else {
                                        RESTExecutionHandler.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) {
                                updateResponseHaders();
                                context.response().end(Buffer.buffer(result));
                            }
                        }
                );


    }

    protected void updateResponseHaders() {
        Optional.ofNullable(headers).ifPresent(h -> h.entrySet().stream().forEach(entry -> context.response().putHeader(entry.getKey(), entry.getValue())));
    }
}
