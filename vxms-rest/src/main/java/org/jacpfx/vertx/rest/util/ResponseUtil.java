package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import org.jacpfx.common.ExecutionResult;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        ResponseUtil.handleError(errorHandler, event.cause());
        createResponse(retryTemp, _timeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer);
    }

    public static <T> void handleExecutionError(Future<T> errorResult, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Throwable e) {
        ResponseUtil.handleError(errorHandler, e);
        try {
            if (onFailureRespond != null) {
                onFailureRespond.accept(e, errorResult);
            } else {
                errorMethodHandler.accept(e);
            }
        } catch (Throwable throwable) {
            errorResult.fail(throwable);
        }
    }

    protected static <T> void executeAndCompleate(ThrowableFutureConsumer<T> userOperation, Future<T> operationResult) {

        try {
            userOperation.accept(operationResult);
        } catch (Throwable throwable) {
            operationResult.fail(throwable);
        }
    }

    public static void handleError(Consumer<Throwable> errorHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }

    }

    public static Map<String, String> updateContentType(Map<String, String> header, String contentType) {
        Map<String, String> headerMap = new HashMap<>(header);
        headerMap.put("content-type", contentType);
        return headerMap;
    }

    public static void sendObjectResult(Object val, HttpServerResponse handler) {
        if (val instanceof String) {
            handler.end(String.valueOf(val));
        } else {
            handler.end(Buffer.buffer((byte[]) val));
        }
    }

    public static void updateHeaderAndStatuscode(Map<String, String> headers,int statuscode, HttpServerResponse response) {
        updateResponseHaders(headers, response);
        updateResponseStatusCode(statuscode, response);
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
