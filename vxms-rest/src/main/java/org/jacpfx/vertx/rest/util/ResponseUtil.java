package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jacpfx.common.ExecutionResult;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.07.16.
 */
public class ResponseUtil {
    public static <T> void createResponse(int _retry, long _timeout, ThrowableFutureConsumer<T> _userOperation,
                                          Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                          Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer) {

        final Future<T> operationResult = Future.future();
        operationResult.setHandler(event -> {
            if (event.failed()) {
                int retryTemp = _retry - 1;
                if (retryTemp < 0) {
                    errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event);
                } else {
                    retry(_timeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer, event, retryTemp);
                }
            } else {
                resultConsumer.accept(new ExecutionResult(event.result(), true, null));
            }
        });
        if (_timeout > 0L) {
            addTimeoutHandler(_timeout, vertx, (l) -> {
                if (!operationResult.isComplete()) {
                    operationResult.fail(new TimeoutException("operation timeout"));
                }
            });
            executeAndCompleate(_userOperation, operationResult);
        } else {
            executeAndCompleate(_userOperation, operationResult);

        }

    }

    protected static void addTimeoutHandler(long _timeout, Vertx vertx, Handler<Long> longHandler) {
        vertx.setTimer(_timeout, longHandler);
    }

    protected static <T> void errorHandling(Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event) {
        try {
            final Future<T> errorResult = Future.future();
            errorResult.setHandler(resultHandler -> {
                if (resultHandler.succeeded()) {
                    resultConsumer.accept(new ExecutionResult(resultHandler.result(), true, null));
                } else {
                    // resultConsumer.accept(new ExecutionResult(null, false, resultHandler.cause()));
                    handleExecutionError(null, errorHandler, null, errorMethodHandler, resultHandler.cause());
                }
            });
            handleExecutionError(errorResult, errorHandler, onFailureRespond, errorMethodHandler, event.cause());

        } catch (Exception e) {
            resultConsumer.accept(new ExecutionResult(null, false, e));
        }
    }

    protected static <T> void retry(long _timeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event, int retryTemp) {
        RESTExecutionUtil.handleError(errorHandler, event.cause());
        createResponse(retryTemp, _timeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer);
    }

    public static <T> void handleExecutionError(Future<T> errorResult, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Throwable e) {
        RESTExecutionUtil.handleError(errorHandler, e);
        if (onFailureRespond != null) {
            try {
                onFailureRespond.accept(e, errorResult);
            } catch (Throwable throwable) {
                errorResult.fail(throwable);
            }
        } else {
            errorMethodHandler.accept(e);
        }

    }

    protected static <T> void executeAndCompleate(ThrowableFutureConsumer<T> userOperation, Future<T> operationResult) {

        try {
            userOperation.accept(operationResult);
        } catch (Throwable throwable) {
            operationResult.fail(throwable);
        }
    }


}
