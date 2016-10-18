package org.jacpfx.vertx.rest.util;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.exceptions.EndpointExecutionException;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 19.01.16.
 */
// TODO Refactor class similar to ResponseUtil
public class ResponseAsyncUtil {

    public static <T> void executeRetryAndCatchAsync(ThrowableSupplier<T> supplier, Future<T> handler, Consumer<Throwable> errorHandler, Function<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, int _retry, long timeout, long delay) {
        T result = null;
        boolean errorHandling = false;
        while (_retry >= 0) {
            errorHandling = false;
            try {
                if (timeout > 0L) {
                    final Future<T> operationResult = Future.future();
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
                    _retry = -1;
                } else {
                    result = supplier.get();
                    _retry = -1;
                }

            } catch (Throwable e) {
                _retry--;
                if (_retry < 0) {
                    // TODO handle exceptions in onErrorCode
                    result = handleError(result, errorHandler, onFailureRespond, errorMethodHandler, e);
                    errorHandling = true;
                } else {
                    ResponseUtil.handleError(errorHandler, e);
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
        try {
            if(delay>0L)Thread.sleep(delay);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
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





}
