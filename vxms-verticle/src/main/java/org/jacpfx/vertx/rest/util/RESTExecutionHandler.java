package org.jacpfx.vertx.rest.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import org.jacpfx.common.exceptions.EndpointExecutionException;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 19.01.16.
 */
public class RESTExecutionHandler {

    public static void sendObjectResult(Object val, HttpServerResponse handler) {
        if (val instanceof String) {
            handler.end(String.valueOf(val));
        } else {
            handler.end(Buffer.buffer((byte[]) val));
        }
    }


    public static  <T> T handleError(HttpServerResponse handler, T result, Consumer<Throwable> errorHandler, Function<Throwable, T> errorFunction, Throwable e) {
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

    public static  void handleError(Consumer<Throwable> errorHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }

    }
}
