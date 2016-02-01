package org.jacpfx.vertx.rest.util;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.exceptions.EndpointExecutionException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 19.01.16.
 */
public class RESTExecutionHandler {

    public static <T> void executeRetryAndCatchAsync(HttpServerResponse response, ThrowableSupplier<T> supplier, Future<T> handler, Consumer<Throwable> errorHandler, Function<Throwable, T> errorFunction, Consumer<Throwable> errorMethodHandler, Vertx vertx, int retry, long timeout, long delay) {
        T result = null;
        boolean errorHandling = false;
        while (retry >= 0) {
            errorHandling = false;
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
                    result = RESTExecutionHandler.handleError(response, result, errorHandler, errorFunction, errorMethodHandler, e);
                    errorHandling = true;
                } else {
                    RESTExecutionHandler.handleError(errorHandler, e);
                    handleDelay(delay);
                }
            }
        }
        if(errorHandling && result==null) handler.fail(new EndpointExecutionException("error...")); // TODO define Error
        if (!handler.isComplete()) handler.complete(result);
    }

    private static void handleDelay(long delay) {
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


    public static  <T> T handleError(HttpServerResponse handler, T result, Consumer<Throwable> errorHandler, Function<Throwable, T> errorFunction,Consumer<Throwable> errorMethodHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }
        if (errorFunction != null) {
            result = errorFunction.apply(e);
        }
        if (errorHandler == null && errorFunction == null) {
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

    public static HttpServerResponse getHttpServerResponse(int httpStatusCode,HttpServerResponse response) {
        if (httpStatusCode != 0) {
            response = response.setStatusCode(httpStatusCode);
        }
        return response;
    }
}
