/*
 * Copyright [2018] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.event.response.basic;

import io.vertx.core.*;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import org.jacpfx.vxms.common.ExecutionResult;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.concurrent.LocalData;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.07.16. Performs Executions and prepares response
 */
public class ResponseExecution {

    private static final int DEFAULT_VALUE = 0;
    private static final long DEFAULT_LONG_VALUE = 0L;
    private static final int DEFAULT_LOCK_TIMEOUT = 2000;
    private static final long LOCK_VALUE = -1L;

    /**
     * Executes the response creation and handles failures
     *
     * @param methodId              the method name/id to be executed
     * @param retry                 the amount of retries
     * @param timeout               the max timeout time for the method execution
     * @param circuitBreakerTimeout the stateful circuit breaker release time
     * @param userOperation         the operation to execute, defined in fluent API
     * @param errorHandler          the intermediate error method, executed on each error
     * @param onFailureRespond      the method to be executed on failure
     * @param errorMethodHandler    the fallback method
     * @param vxmsShared            the vxmsShared instance, containing the Vertx instance and other shared
     *                              objects per instance
     * @param failure               last thrown Exception
     * @param resultConsumer        the consumer that takes the execution resultz
     * @param <T>                   the type of response
     */
    public static <T> void createResponse(
            String methodId,
            int retry,
            long timeout,
            long circuitBreakerTimeout,
            ThrowableFutureConsumer<T> userOperation,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            VxmsShared vxmsShared,
            Throwable failure,
            Consumer<ExecutionResult<T>> resultConsumer) {

        if (circuitBreakerTimeout > DEFAULT_LONG_VALUE) {
            executeStateful(
                    methodId,
                    retry,
                    timeout,
                    circuitBreakerTimeout,
                    userOperation,
                    errorHandler,
                    onFailureRespond,
                    errorMethodHandler,
                    vxmsShared,
                    failure,
                    resultConsumer);
        } else {
            executeStateless(
                    methodId,
                    retry,
                    timeout,
                    circuitBreakerTimeout,
                    userOperation,
                    errorHandler,
                    onFailureRespond,
                    errorMethodHandler,
                    vxmsShared,
                    resultConsumer);
        }
    }

    private static <T> void executeStateless(
            String _methodId,
            int _retry,
            long _timeout,
            long _release,
            ThrowableFutureConsumer<T> _userOperation,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            VxmsShared vxmsShared,
            Consumer<ExecutionResult<T>> resultConsumer) {
        final Promise<T> operationResult = Promise.promise();
        final Future<T> operationResultFuture = operationResult.future();
        operationResultFuture.onComplete(
                event -> {
                    if (event.failed()) {
                        int retryTemp = _retry - 1;
                        retryOrFail(
                                _methodId,
                                _timeout,
                                _release,
                                _userOperation,
                                errorHandler,
                                onFailureRespond,
                                errorMethodHandler,
                                vxmsShared,
                                resultConsumer,
                                event,
                                retryTemp);
                    } else {
                        resultConsumer.accept(new ExecutionResult<>(event.result(), true, null));
                    }
                });
        if (_timeout > DEFAULT_LONG_VALUE) {
            addTimeoutHandler(
                    _timeout,
                    vxmsShared,
                    (l) -> {
                        if (!operationResultFuture.isComplete()) {
                            operationResult.fail(new TimeoutException("operation timeout"));
                        }
                    });
        }
        executeAndCompleate(_userOperation, operationResult);
    }

    private static <T> void executeAndCompleate(
            ThrowableFutureConsumer<T> userOperation, Promise<T> operationResult) {

        try {
            userOperation.accept(operationResult);
        } catch (Throwable throwable) {
            operationResult.fail(throwable);
        }
    }

    private static <T> void retryOrFail(
            String _methodId,
            long _timeout,
            long _release,
            ThrowableFutureConsumer<T> _userOperation,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            VxmsShared vxmsShared,
            Consumer<ExecutionResult<T>> resultConsumer,
            AsyncResult<T> event,
            int retryTemp) {
        if (retryTemp < DEFAULT_VALUE) {
            errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event);
        } else {
            retry(
                    _methodId,
                    retryTemp,
                    _timeout,
                    _release,
                    _userOperation,
                    errorHandler,
                    onFailureRespond,
                    errorMethodHandler,
                    vxmsShared,
                    resultConsumer,
                    event);
        }
    }

    private static <T> void executeStateful(
            String _methodId,
            int _retry,
            long _timeout,
            long _circuitBreakerTimeout,
            ThrowableFutureConsumer<T> _userOperation,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            VxmsShared vxmsShared,
            Throwable t,
            Consumer<ExecutionResult<T>> resultConsumer) {
        final Promise<T> operationResult = Promise.promise();
        final Future<T> operationResultFuture = operationResult.future();
        operationResultFuture.onComplete(
                event -> {
                    if (event.failed()) {
                        statefulErrorHandling(
                                _methodId,
                                _retry,
                                _timeout,
                                _circuitBreakerTimeout,
                                _userOperation,
                                errorHandler,
                                onFailureRespond,
                                errorMethodHandler,
                                vxmsShared,
                                resultConsumer,
                                event);
                    } else {
                        resultConsumer.accept(new ExecutionResult<>(event.result(), true, null));
                    }
                });

        executeLocked(
                (lock, counter) ->
                        counter.get(
                                counterHandler -> {
                                    long currentVal = counterHandler.result();
                                    if (currentVal == DEFAULT_LONG_VALUE) {
                                        executeInitialState(
                                                _retry,
                                                _timeout,
                                                _userOperation,
                                                vxmsShared,
                                                operationResult,
                                                lock,
                                                counter);
                                    } else if (currentVal > DEFAULT_LONG_VALUE) {
                                        executeDefaultState(
                                                _timeout, _userOperation, vxmsShared, operationResult, lock);
                                    } else {
                                        releaseLockAndHandleError(
                                                errorHandler,
                                                onFailureRespond,
                                                errorMethodHandler,
                                                resultConsumer,
                                                lock,
                                                Optional.ofNullable(t).orElse(Future.failedFuture("circuit open").cause()));
                                    }
                                }),
                _methodId,
                vxmsShared,
                errorHandler,
                onFailureRespond,
                errorMethodHandler,
                resultConsumer);
    }

    private static <T> void releaseLockAndHandleError(
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            Consumer<ExecutionResult<T>> resultConsumer,
            Lock lock,
            Throwable cause) {
        Optional.ofNullable(lock).ifPresent(Lock::release);
        errorHandling(
                errorHandler,
                onFailureRespond,
                errorMethodHandler,
                resultConsumer,
                Future.failedFuture(cause));
    }

    private static <T> void executeDefaultState(
            long _timeout,
            ThrowableFutureConsumer<T> _userOperation,
            VxmsShared vxmsShared,
            Promise<T> operationResult,
            Lock lock) {
        lock.release();
        if (_timeout > DEFAULT_LONG_VALUE) {
            addTimeoutHandler(
                    _timeout,
                    vxmsShared,
                    (l) -> {
                        if (!operationResult.future().isComplete()) {
                            operationResult.fail(new TimeoutException("operation timeout"));
                        }
                    });
        }
        executeAndCompleate(_userOperation, operationResult);
    }

    private static <T> void executeInitialState(
            int _retry,
            long _timeout,
            ThrowableFutureConsumer<T> _userOperation,
            VxmsShared vxmsShared,
            Promise<T> operationResult,
            Lock lock,
            Counter counter) {
        final long initialRetryCounterValue = _retry + 1;
        counter.addAndGet(
                initialRetryCounterValue,
                rHandler ->
                        executeDefaultState(_timeout, _userOperation, vxmsShared, operationResult, lock));
    }

    private static <T> void statefulErrorHandling(
            String _methodId,
            int _retry,
            long _timeout,
            long _circuitBreakerTimeout,
            ThrowableFutureConsumer<T> _userOperation,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            VxmsShared vxmsShared,
            Consumer<ExecutionResult<T>> resultConsumer,
            AsyncResult<T> event) {

        executeLocked(
                (lock, counter) ->
                        counter.decrementAndGet(
                                valHandler -> {
                                    if (valHandler.succeeded()) {
                                        handleStatefulError(
                                                _methodId,
                                                _retry,
                                                _timeout,
                                                _circuitBreakerTimeout,
                                                _userOperation,
                                                errorHandler,
                                                onFailureRespond,
                                                errorMethodHandler,
                                                vxmsShared,
                                                resultConsumer,
                                                event,
                                                lock,
                                                counter,
                                                valHandler);
                                    } else {
                                        releaseLockAndHandleError(
                                                errorHandler,
                                                onFailureRespond,
                                                errorMethodHandler,
                                                resultConsumer,
                                                lock,
                                                valHandler.cause());
                                    }
                                }),
                _methodId,
                vxmsShared,
                errorHandler,
                onFailureRespond,
                errorMethodHandler,
                resultConsumer);
    }

    private static <T> void handleStatefulError(
            String _methodId,
            int _retry,
            long _timeout,
            long _circuitBreakerTimeout,
            ThrowableFutureConsumer<T> _userOperation,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            VxmsShared vxmsShared,
            Consumer<ExecutionResult<T>> resultConsumer,
            AsyncResult<T> event,
            Lock lock,
            Counter counter,
            AsyncResult<Long> valHandler) {
        long count = valHandler.result();
        if (count <= DEFAULT_LONG_VALUE) {
            setCircuitBreakerReleaseTimer(_retry, _circuitBreakerTimeout, vxmsShared, counter);
            openCircuitBreakerAndHandleError(
                    errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event, lock, counter);
        } else {
            lock.release();
            retry(
                    _methodId,
                    _retry,
                    _timeout,
                    _circuitBreakerTimeout,
                    _userOperation,
                    errorHandler,
                    onFailureRespond,
                    errorMethodHandler,
                    vxmsShared,
                    resultConsumer,
                    event);
        }
    }

    private static <T> void openCircuitBreakerAndHandleError(
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            Consumer<ExecutionResult<T>> resultConsumer,
            AsyncResult<T> event,
            Lock lock,
            Counter counter) {
        counter.addAndGet(
                LOCK_VALUE,
                val -> {
                    lock.release();
                    errorHandling(
                            errorHandler,
                            onFailureRespond,
                            errorMethodHandler,
                            resultConsumer,
                            Future.failedFuture(event.cause()));
                });
    }

    private static void setCircuitBreakerReleaseTimer(
            int _retry, long _release, VxmsShared vxmsShared, Counter counter) {
        final long initialRetryCounterValue = _retry + 1;
        final Vertx vertx = vxmsShared.getVertx();
        vertx.setTimer(_release, timer -> counter.addAndGet(initialRetryCounterValue, val -> {
        }));
    }

    private static void addTimeoutHandler(
            long _timeout, VxmsShared vxmsShared, Handler<Long> longHandler) {
        final Vertx vertx = vxmsShared.getVertx();
        vertx.setTimer(_timeout, longHandler);
    }

    private static <T> void errorHandling(
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            Consumer<ExecutionResult<T>> resultConsumer,
            AsyncResult<T> event) {
        try {
            final Promise<T> errorPromise = Promise.promise();
            final Future<T> errorResult = errorPromise.future();
            errorResult.onComplete(
                    resultHandler -> {
                        if (resultHandler.succeeded()) {
                            resultConsumer.accept(
                                    new ExecutionResult<>(resultHandler.result(), true, true, null));
                        } else {
                            handleExecutionError(
                                    null, errorHandler, null, errorMethodHandler, resultHandler.cause());
                        }
                    });
            handleExecutionError(
                    errorPromise, errorHandler, onFailureRespond, errorMethodHandler, event.cause());

        } catch (Exception e) {
            resultConsumer.accept(new ExecutionResult<>(null, false, e));
        }
    }

    private static <T> void retry(
            String _methodId,
            int retryTemp,
            long _timeout,
            long _release,
            ThrowableFutureConsumer<T> _userOperation,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            VxmsShared vxmsShared,
            Consumer<ExecutionResult<T>> resultConsumer,
            AsyncResult<T> event) {
        ResponseExecution.handleError(errorHandler, event.cause());
        createResponse(
                _methodId,
                retryTemp,
                _timeout,
                _release,
                _userOperation,
                errorHandler,
                onFailureRespond,
                errorMethodHandler,
                vxmsShared,
                null,
                resultConsumer);
    }

    private static <T> void handleExecutionError(
            Promise<T> errorPromise,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            Throwable e) {
        ResponseExecution.handleError(errorHandler, e);
        try {
            if (onFailureRespond != null && errorPromise != null) {
                onFailureRespond.accept(e, errorPromise);
            } else {
                errorMethodHandler.accept(e);
            }
        } catch (Throwable throwable) {
            if (errorPromise == null) {
                throwable.printStackTrace();
                return;
            }
            errorPromise.fail(throwable);
        }
    }

    public static void handleError(Consumer<Throwable> errorHandler, Throwable e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Optional<?> encode(Serializable value, Encoder encoder) {
        try {
            if (encoder instanceof Encoder.ByteEncoder) {
                return Optional.ofNullable(((Encoder.ByteEncoder) encoder).encode(value));
            } else if (encoder instanceof Encoder.StringEncoder) {
                return Optional.ofNullable(((Encoder.StringEncoder) encoder).encode(value));
            } else {
                return Optional.ofNullable(value);
            }

        } catch (Exception e) {
            // TODO ignore serialisation currently... log message
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private static <T> void executeLocked(
            LockedConsumer consumer,
            String _methodId,
            VxmsShared vxmsShared,
            Consumer<Throwable> errorHandler,
            ThrowableErrorConsumer<Throwable, T> onFailureRespond,
            Consumer<Throwable> errorMethodHandler,
            Consumer<ExecutionResult<T>> resultConsumer) {
        final LocalData sharedData = vxmsShared.getLocalData();
        sharedData.getLockWithTimeout(
                _methodId,
                DEFAULT_LOCK_TIMEOUT,
                lockHandler -> {
                    final Lock lock = lockHandler.result();
                    if (lockHandler.succeeded()) {
                        sharedData.getCounter(
                                _methodId,
                                resultHandler -> {
                                    if (resultHandler.succeeded()) {
                                        consumer.execute(lock, resultHandler.result());
                                    } else {
                                        releaseLockAndHandleError(
                                                errorHandler,
                                                onFailureRespond,
                                                errorMethodHandler,
                                                resultConsumer,
                                                lock,
                                                resultHandler.cause());
                                    }
                                });
                    } else {
                        releaseLockAndHandleError(
                                errorHandler,
                                onFailureRespond,
                                errorMethodHandler,
                                resultConsumer,
                                lock,
                                lockHandler.cause());
                    }
                });
    }

    private interface LockedConsumer {

        void execute(Lock lock, Counter counter);
    }
}
