package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.exceptions.EndpointExecutionException;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicResponse {
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final Map<String, String> headers;
    private final boolean async;
    private final ThrowableSupplier<byte[]> byteSupplier;
    private final ThrowableSupplier<String> stringSupplier;
    private final ThrowableSupplier<Serializable> objectSupplier;
    private final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Function<Throwable, byte[]> errorHandlerByte;
    protected final Function<Throwable, String> errorHandlerString;
    protected final Function<Throwable, Serializable> errorHandlerObject;
    protected final int retryCount;

    public ExecuteRSBasicResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, ThrowableSupplier<String> stringSupplier, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, Function<Throwable, String> errorHandlerString, Function<Throwable, Serializable> errorHandlerObject, int retryCount) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.async = async;
        this.byteSupplier = byteSupplier;
        this.stringSupplier = stringSupplier;
        this.objectSupplier = objectSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorHandlerByte = errorHandlerByte;
        this.errorHandlerString = errorHandlerString;
        this.errorHandlerObject = errorHandlerObject;
        this.retryCount = retryCount;
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerByte the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasic onByteResponseError(Function<Throwable, byte[]> errorHandlerByte) {
        return new ExecuteRSBasic(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerString the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasic onStringResponseError(Function<Throwable, String> errorHandlerString) {
        return new ExecuteRSBasic(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate response value
     *
     * @param errorHandlerObject the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasic onObjectResponseError(Function<Throwable, Serializable> errorHandlerObject) {
        return new ExecuteRSBasic(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    public ExecuteRSBasicResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    public ExecuteRSBasicResponse retry(int retryCount) {
        return new ExecuteRSBasicResponse(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, stringSupplier, objectSupplier, encoder, errorHandler, errorHandlerByte, errorHandlerString, errorHandlerObject, retryCount);
    }

    public void execute() {

        Optional.ofNullable(stringSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            String result = "";
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();

                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = handleError(context.response(), result, errorHandler, errorHandlerString, e);
                                    } else {
                                        handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) context.response().end(result);

                        }
                );

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
                                        result = handleError(context.response(), result, errorHandler, errorHandlerByte, e);
                                    } else {
                                        handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) context.response().end(Buffer.buffer(result));
                        }
                );

        Optional.ofNullable(objectSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            Serializable result = "";
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();
                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = handleError(context.response(), result, errorHandler, errorHandlerObject, e);
                                    } else {
                                        handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended())
                                sendObjectResult(WebSocketExecutionUtil.encode(result, encoder), context.response());
                        }
                );


    }

    public static void sendObjectResult(Object val, HttpServerResponse handler) {
        if (val instanceof String) {
            handler.end(String.valueOf(val));
        } else {
            handler.end(Buffer.buffer((byte[]) val));
        }
    }


    private static <T> T handleError(HttpServerResponse handler, T result, Consumer<Throwable> errorHandler, Function<Throwable, T> errorFunction, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }
        if (errorFunction != null) {
            result = errorFunction.apply(e);
        }
        if (errorHandler == null && errorFunction == null) {
            handler.setStatusCode(500).end(new EndpointExecutionException(e).getMessage());
        }
        return result;
    }

    private static void handleError(Consumer<Throwable> errorHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }

    }
}
