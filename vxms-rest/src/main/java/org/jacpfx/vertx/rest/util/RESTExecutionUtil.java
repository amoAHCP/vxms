package org.jacpfx.vertx.rest.util;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.common.exceptions.EndpointExecutionException;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 19.01.16.
 */
// TODO Refactor class similar to ResponseUtil
public class RESTExecutionUtil {

    public static <T> void executeRetryAndCatchAsync(ThrowableSupplier<T> supplier, Future<T> handler, Consumer<Throwable> errorHandler, Function<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, int retry, long timeout, long delay) {
        T result = null;
        boolean errorHandling = false;
        while (retry >= 0) {
            errorHandling = false;
            try {
                if (timeout > 0L) {
                    Future<T> operationResult = Future.future();
                    vertx.setTimer(timeout, (l) -> {
                        if (!operationResult.isComplete()) {
                            operationResult.fail(new TimeoutException("operation timeout"));
                        }
                    });

                    executeAndCompleate(supplier, operationResult);

                    if(!operationResult.failed()) {
                        result = operationResult.result();
                    } else {
                        throw  operationResult.cause();
                    }
                    retry = -1;
                } else {
                    result = supplier.get();
                    retry = -1;
                }

            } catch (Throwable e) {
                retry--;
                if (retry < 0) {
                    // TODO handle exceptions in onErrorCode
                    result = handleError(result, errorHandler, onFailureRespond, errorMethodHandler, e);
                    errorHandling = true;
                } else {
                    handleError(errorHandler, e);
                    handleDelay(delay);
                }
            }
        }
        if(errorHandling && result==null) handler.fail(new EndpointExecutionException("error...")); // TODO define Error
        if (!handler.isComplete()) handler.complete(result);
    }

    protected static <T> void executeAndCompleate(ThrowableSupplier<T> supplier,  Future<T> operationResult) {
        T temp = null;
        try {
            temp = supplier.get();
        } catch (Throwable throwable) {
            operationResult.fail(throwable);
        }
        if(!operationResult.failed())operationResult.complete(temp);
    }


    private static void handleDelay(long delay) {
        // TODO switch to timer implementation
        try {
            if(delay>0L)Thread.sleep(delay);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public static void sendObjectResult(Object val, HttpServerResponse handler) {
        if (val instanceof String) {
            handler.end(String.valueOf(val));
        } else {
            handler.end(Buffer.buffer((byte[]) val));
        }
    }


    public static  <T> T handleError(T result, Consumer<Throwable> errorHandler, Function<Throwable, T> onFailureRespond,Consumer<Throwable> errorMethodHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }
        if (onFailureRespond != null) {
            result = onFailureRespond.apply(e);
        }
        if (errorHandler == null && onFailureRespond == null) {
            errorMethodHandler.accept(e);
            return null;

        }
        return result;
    }

    public static  void handleError(Consumer<Throwable> errorHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }

    }


    public static void updateResponseHaders(Map<String, String> headers, HttpServerResponse response) {
        Optional.ofNullable(headers).ifPresent(h -> h.entrySet().stream().forEach(entry -> response.putHeader(entry.getKey(), entry.getValue())));
    }

    public static void updateResponseStatusCode(int httpStatusCode, HttpServerResponse response) {
        if (httpStatusCode != 0) {
            response.setStatusCode(httpStatusCode);
        }
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
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
