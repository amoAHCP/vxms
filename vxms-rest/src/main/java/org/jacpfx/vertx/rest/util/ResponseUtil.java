package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jacpfx.common.ExecutionResult;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.ThrowableSupplier;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

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
                  handleError(null, errorHandler, null, errorMethodHandler, resultHandler.cause());
                }
            });
            handleError(errorResult, errorHandler, onFailureRespond, errorMethodHandler, event.cause());

        } catch (Exception e) {
            resultConsumer.accept(new ExecutionResult(null, false, e));
        }
    }

    protected static <T> void retry(long _timeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event, int retryTemp) {
        try {
            RESTExecutionUtil.handleError(errorHandler, event.cause());
        } catch (Exception e) {
            // no error handling needed
        }
        createResponse(retryTemp, _timeout,  _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer);
    }

    public static <T> void handleError(Future<T> errorResult, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Throwable e) {
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

    @Deprecated
    public static <T> T createResponse(int retry, T result, ThrowableSupplier<T> supplier, Consumer<Throwable> errorHandler, Function<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, long timeout) {
        while (retry >= 0) {
            try {
                if (timeout > 0L) {
                    Future<T> operationResult = Future.future();
                    // TODO this is not working
                    addTimeoutHandler(timeout, vertx, (l) -> {
                        if (!operationResult.isComplete()) {
                            operationResult.fail(new TimeoutException("operation timeout"));
                        }
                    });

                    executeAndCompleate(supplier, operationResult);

                    if (!operationResult.failed()) {
                        result = operationResult.result();
                    } else {
                        throw operationResult.cause();
                    }
                    retry = -1;
                } else {
                    result = supplier.get();
                    retry = -1;
                }
            } catch (Throwable e) {
                retry--;
                if (retry < 0) {
                    result = RESTExecutionUtil.handleError(result, errorHandler, onFailureRespond, errorMethodHandler, e);
                } else {
                    RESTExecutionUtil.handleError(errorHandler, e);
                }
            }
        }

        return result;
    }

    protected static <T> void executeAndCompleate(ThrowableSupplier<T> supplier, Future<T> operationResult) {
        T temp = null;
        try {
            temp = supplier.get();
        } catch (Throwable throwable) {
            operationResult.fail(throwable);
        }
        if (!operationResult.failed()) operationResult.complete(temp);
    }
}
