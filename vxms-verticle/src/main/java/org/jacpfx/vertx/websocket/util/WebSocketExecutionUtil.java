package org.jacpfx.vertx.websocket.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.exceptions.EndpointExecutionException;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 01.12.15.
 */
public class WebSocketExecutionUtil {

    public static <T> T
    executeRetryAndCatchAsync(ThrowableSupplier<T> supplier, Future<T> handler, T result, Consumer<Throwable> errorHandler, Function<Throwable, T> errorFunction, Vertx vertx, int retry, long timeout, long delay) {


        while (retry >= 0) {

            try {
                if (timeout > 0L) {
                    final CompletableFuture<T> timeoutFuture = new CompletableFuture();
                    vertx.executeBlocking((innerHandler) -> {
                        T temp = null;

                        try {
                            temp = supplier.get();
                        } catch (Throwable throwable) {
                            timeoutFuture.obtrudeException(throwable);
                        }
                        timeoutFuture.complete(temp);
                    }, false, (val) -> {

                    });
                    result = timeoutFuture.get(timeout, TimeUnit.MILLISECONDS);
                    retry = -1;
                } else {
                    result = supplier.get();
                    retry = -1;
                }

            } catch (Throwable e) {
                retry--;
                if (retry < 0) {
                    result = handleError(handler, result, errorHandler, errorFunction, e);
                } else {
                    handleDelay(delay);
                }
            }
        }
        if (!handler.isComplete()) handler.complete(result);
        return result;
    }

    private static void handleDelay(long delay) {
        try {
            if(delay>0L)Thread.sleep(delay);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private static <T> T handleError(Future<T> handler, T result, Consumer<Throwable> errorHandler, Function<Throwable, T> errorFunction, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }
        if (errorFunction != null) {
            result = errorFunction.apply(e);
        }
        if (errorHandler == null && errorFunction == null) {
            handler.fail(new EndpointExecutionException(e));
        }
        return result;
    }

    public static <T> T executeRetryAndCatch(ThrowableSupplier<T> supplier, T result, Consumer<Throwable> errorHandler, Function<Throwable, T> errorFunction, Consumer<Throwable> errorMethodHandler, int retry) {
        while (retry >= 0) {

            try {
                result = supplier.get();
                retry = -1;
            } catch (Throwable e) {
                retry--;
                if (retry < 0) {
                    if (errorHandler != null) {
                        errorHandler.accept(e);
                    }
                    if (errorFunction != null) {
                        result = errorFunction.apply(e);
                    }
                    if (errorHandler == null && errorFunction == null) {
                        errorMethodHandler.accept(e);
                    }
                }
            }
        }
        return result;
    }

    public static Optional<?> encode(Serializable value, Encoder encoder) {
        try {
            if (encoder instanceof Encoder.ByteEncoder) {
                return Optional.ofNullable(((Encoder.ByteEncoder) encoder).encode(value));
            } else if (encoder instanceof Encoder.StringEncoder) {
                return Optional.ofNullable(((Encoder.StringEncoder) encoder).encode(value));
            }

        } catch (Exception e) {
            // TODO ignore serialisation currently... log message
        }

        return Optional.empty();
    }

    public static  void sendText(CommType commType, Vertx vertx, WebSocketRegistry registry, WebSocketEndpoint[] endpoint, String value) {
        final WebSocketEndpoint currentEndpoint = endpoint[0];
        if(currentEndpoint==null) return; // TODO define Exception!!!!
        switch (commType) {

            case ALL:
                registry.findEndpointsAndExecute(currentEndpoint, match -> vertx.eventBus().send(match.getTextHandlerId(), value));
                break;
            case ALL_BUT_CALLER:
                registry.findEndpointsAndExecute(currentEndpoint, match -> {
                    if (!currentEndpoint.equals(match)) vertx.eventBus().send(match.getTextHandlerId(), value);
                });
                break;
            case CALLER:
                vertx.eventBus().send(currentEndpoint.getTextHandlerId(), value);
                break;
            case TO:
                Stream.of(endpoint).forEach(ep -> vertx.eventBus().send(ep.getTextHandlerId(), value));
                break;
        }
    }

    public static  void sendBinary(CommType commType, Vertx vertx,WebSocketRegistry registry,WebSocketEndpoint[] endpoint, byte[] value) {
        final WebSocketEndpoint currentEndpoint = endpoint[0];
        if(currentEndpoint==null) return; // TODO define Exception!!!!
        switch (commType) {

            case ALL:
                registry.findEndpointsAndExecute(currentEndpoint, match -> vertx.eventBus().send(match.getBinaryHandlerId(), Buffer.buffer(value)));
                break;
            case ALL_BUT_CALLER:
                registry.findEndpointsAndExecute(currentEndpoint, match -> {
                    if (!currentEndpoint.equals(match))
                        vertx.eventBus().send(match.getBinaryHandlerId(), Buffer.buffer(value));
                });
                break;
            case CALLER:
                vertx.eventBus().send(currentEndpoint.getBinaryHandlerId(), Buffer.buffer(value));
                break;
            case TO:
                Stream.of(endpoint).forEach(ep -> vertx.eventBus().send(ep.getBinaryHandlerId(), Buffer.buffer(value)));
                break;
        }
    }

    public static void sendObjectResult(Object val, CommType commType, Vertx vertx, WebSocketRegistry registry, WebSocketEndpoint[] endpoint) {
        if (val instanceof String) {
            sendText(commType, vertx, registry, endpoint, (String) val);
        } else {
            sendBinary(commType, vertx, registry, endpoint, (byte[]) val);
        }
    }

    public static void handleExecutionResult(AsyncResult<?> result, Consumer<Throwable> errorMethodHandler, Runnable r) {
        if (result.failed()) {
            errorMethodHandler.accept(result.cause().getCause());
        } else {
            r.run();
        }
    }

}
