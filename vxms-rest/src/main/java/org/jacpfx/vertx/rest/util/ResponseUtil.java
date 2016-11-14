package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
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
    public static <T> void createResponse(String _methodId, int _retry, long _timeout, long _circuitBreakerTimeout, ThrowableFutureConsumer<T> _userOperation,
                                          Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond,
                                          Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer) {

        if (_circuitBreakerTimeout > 0) {
            executeStateful(_methodId, _retry, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer);
        } else {
            executeStateless(_methodId, _retry, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer);
        }
    }

    private static <T> void executeStateless(String _methodId, int _retry, long _timeout, long _circuitBreakerTimeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer) {
        final Future<T> operationResult = Future.future();
        operationResult.setHandler(event -> {
            if (event.failed()) {
                int retryTemp = _retry - 1;
                retryOrFail(_methodId, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer, event, retryTemp);
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

    private static <T> void executeAndCompleate(ThrowableFutureConsumer<T> userOperation, Future<T> operationResult) {

        try {
            userOperation.accept(operationResult);
        } catch (Throwable throwable) {
            operationResult.fail(throwable);
        }
    }

    private static <T> void retryOrFail(String _methodId, long _timeout, long _circuitBreakerTimeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event, int retryTemp) {
        if (retryTemp < 0) {
            errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event);
        } else {
            retry(_methodId, retryTemp, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer, event);
        }
    }

    private static <T> void executeStateful(String _methodId, int _retry, long _timeout, long _circuitBreakerTimeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer) {
        final Future<T> operationResult = Future.future();
        operationResult.setHandler(event -> {
            if (event.failed()) {
                statefulErrorHandling(_methodId, _retry, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer, event);
            } else {
                resultConsumer.accept(new ExecutionResult(event.result(), true, null));
            }
        });


        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(_methodId, 2000, lockHandler -> {
            if (lockHandler.succeeded()) {
                sharedData.getCounter(_methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        resultHandler.result().get(counterHandler -> {
                            long currentVal = counterHandler.result();
                            if (currentVal == 0) {
                                executeInitialState(_retry, _timeout, _userOperation, vertx, operationResult, lockHandler, resultHandler);
                            } else if (currentVal > 0) {
                                executeDefaultState(_timeout, _userOperation, vertx, operationResult, lockHandler);
                            } else {
                                executeErrorState(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, lockHandler,Future.failedFuture("circuit open"));
                            }
                        });
                    } else {
                        executeErrorState(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, lockHandler,Future.failedFuture(resultHandler.cause()));
                    }
                });
            }else {
                executeErrorState(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, lockHandler,Future.failedFuture(lockHandler.cause()));
            }

        });
    }

    private static <T> void executeErrorState(Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<Lock> lockHandler,AsyncResult<T> event) {
        lockHandler.result().release();
        errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event);
    }

    private static <T> void executeDefaultState(long _timeout, ThrowableFutureConsumer<T> _userOperation, Vertx vertx, Future<T> operationResult, AsyncResult<Lock> lockHandler) {
        lockHandler.result().release();
        if (_timeout > 0L) {
            addTimeoutHandler(_timeout, vertx, (l) -> {
                if (!operationResult.isComplete()) {
                    operationResult.fail(new TimeoutException("operation timeout"));
                }
            });
        }
        executeAndCompleate(_userOperation, operationResult);
    }

    private static <T> void executeInitialState(int _retry, long _timeout, ThrowableFutureConsumer<T> _userOperation, Vertx vertx, Future<T> operationResult, AsyncResult<Lock> lockHandler, AsyncResult<Counter> resultHandler) {
        resultHandler.result().addAndGet(Integer.valueOf(_retry + 1).longValue(), rHandler -> {
            executeDefaultState(_timeout, _userOperation, vertx, operationResult, lockHandler);
        });
    }

    private static <T> void statefulErrorHandling(String _methodId, int _retry, long _timeout, long _circuitBreakerTimeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event) {
        final SharedData sharedData = vertx.sharedData();
        sharedData.getLockWithTimeout(_methodId, 2000, lockHandler -> {
            if (lockHandler.succeeded()) {
                final Lock lock = lockHandler.result();
                sharedData.getCounter(_methodId, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        final Counter counter = resultHandler.result();
                        counter.decrementAndGet(valHandler -> {
                            if (valHandler.succeeded()) {
                                handleStatefulError(_methodId, _retry, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer, event, lock, counter, valHandler);
                            } else {
                                releaseLockAndHandleError(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, lock, valHandler.cause());
                            }
                        });
                    } else {
                        releaseLockAndHandleError(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, lock, resultHandler.cause());
                    }
                });
            } else {
                errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, Future.failedFuture(lockHandler.cause()));
            }
        });
    }

    private static <T> void handleStatefulError(String _methodId, int _retry, long _timeout, long _circuitBreakerTimeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event, Lock lock, Counter counter, AsyncResult<Long> valHandler) {
        long count = valHandler.result();
        if (count <= 0) {
            if (_circuitBreakerTimeout > 0) {
                setCircuitBreakerReleaseTimer(_retry, _circuitBreakerTimeout, vertx, counter);
                openCircuitBreaker(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event, lock, counter);

            } else {
                resetCounterAndHandleError(_retry, errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event, lock, counter);
            }
        } else {
            lock.release();
            retry(_methodId, _retry, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer, event);
        }
    }

    private static <T> void resetCounterAndHandleError(int _retry, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event, Lock lock, Counter counter) {
        counter.addAndGet(Integer.valueOf(_retry + 1).longValue(), val ->
                releaseLockAndHandleError(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, lock, event.cause()));
    }

    private static <T> void openCircuitBreaker(Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event, Lock lock, Counter counter) {
        counter.addAndGet(-1l, val -> {
            lock.release();
            errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, Future.failedFuture(event.cause()));
        });
    }

    private static void setCircuitBreakerReleaseTimer(int _retry, long _release, Vertx vertx, Counter counter) {
        vertx.setTimer(_release, timer ->  counter.addAndGet(Integer.valueOf(_retry + 1).longValue(), val -> {}));
    }

    private static <T> void releaseLockAndHandleError(Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Consumer<ExecutionResult<T>> resultConsumer, Lock lock, Throwable cause) {
        Optional.ofNullable(lock).ifPresent(lck ->lck.release());
        errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, Future.failedFuture(cause));
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
                    handleExecutionError(null, errorHandler, null, errorMethodHandler, resultHandler.cause());
                }
            });
            handleExecutionError(errorResult, errorHandler, onFailureRespond, errorMethodHandler, event.cause());

        } catch (Exception e) {
            resultConsumer.accept(new ExecutionResult(null, false, e));
        }
    }

    protected static <T> void retry(String _methodId, int retryTemp, long _timeout, long _circuitBreakerTimeout, ThrowableFutureConsumer<T> _userOperation, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler, Vertx vertx, Consumer<ExecutionResult<T>> resultConsumer, AsyncResult<T> event) {
        ResponseUtil.handleError(errorHandler, event.cause());
        createResponse(_methodId, retryTemp, _timeout, _circuitBreakerTimeout, _userOperation, errorHandler, onFailureRespond, errorMethodHandler, vertx, resultConsumer);
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

    public static void updateHeaderAndStatuscode(Map<String, String> headers, int statuscode, HttpServerResponse response) {
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
