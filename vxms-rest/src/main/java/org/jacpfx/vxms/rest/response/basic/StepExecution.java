/*
 * Copyright [2017] [Andy Moncsek]
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

package org.jacpfx.vxms.rest.response.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionResult;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.concurrent.LocalData;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;

/**
 * Created by Andy Moncsek on 21.07.16. Contains the method chain to execute the response chaine
 * defined in the fluent API. The class is generic, so it is used for all types of response.
 */
public class StepExecution {

  private static final int DEFAULT_VALUE = 0;
  private static final long DEFAULT_LONG_VALUE = 0L;
  private static final int DEFAULT_LOCK_TIMEOUT = 2000;
  private static final long LOCK_VALUE = -1L;

  /**
   * Creates the response value based on the flow defined in the fluent API.  The resulting response
   * will be passed to an execution consumer.
   *
   * @param methodId, the method name/id to be executed
   * @param retry, the amount of retries
   * @param timeout, the timeout time for execution
   * @param circuitBreakerTimeout, the stateful circuit breaker release time
   * @param step, the user step to be executed
   * @param errorHandler, the intermediate error method, executed on each error
   * @param onFailureRespond, the method to be executed on failure
   * @param errorMethodHandler, the fallback method
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param fail, last thrown Exception
   * @param resultConsumer, the consumer of the {@link ExecutionResult}
   * @param <T> the type of response (String, byte, Object)
   */
  public static <T, V> void createResponse(String methodId,
      ThrowableFutureBiConsumer<T, V> step,
      T inputValue,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable fail,
      Consumer<ExecutionResult<V>> resultConsumer,
      int retry,
      long timeout,
      long circuitBreakerTimeout) {

    if (circuitBreakerTimeout > DEFAULT_LONG_VALUE) {
      executeStateful(methodId,
          retry, timeout,
          circuitBreakerTimeout,
          step,
          inputValue,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          vxmsShared, fail, resultConsumer);
    } else {
      executeStateless(methodId,
          retry,
          timeout,
          circuitBreakerTimeout,
          step,
          inputValue,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          vxmsShared, resultConsumer);
    }
  }

  private static <T, V> void executeStateless(String _methodId,
      int retry,
      long timeout,
      long release,
      ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<V>> resultConsumer) {
    final Future<V> operationResult = Future.future();
    operationResult.setHandler(event -> {
      if (event.failed()) {
        int retryTemp = retry - 1;
        retryOrFail(_methodId,
            timeout,
            release,
            _step,
            _inputValue,
            errorHandler,
            onFailureRespond,
            errorMethodHandler,
            vxmsShared, resultConsumer,
            event, retryTemp);
      } else {
        resultConsumer.accept(new ExecutionResult<>(event.result(), true, null));
      }
    });
    if (timeout > DEFAULT_LONG_VALUE) {
      addTimeoutHandler(timeout, vxmsShared.getVertx(), (l) -> {
        if (!operationResult.isComplete()) {
          operationResult.fail(new TimeoutException("operation timeout"));
        }
      });

    }
    executeAndCompleate(_step, _inputValue, operationResult);


  }

  private static <T, V> void executeAndCompleate(ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      Future<V> operationResult) {

    try {
      _step.accept(_inputValue, operationResult);
    } catch (Throwable throwable) {
      operationResult.fail(throwable);
    }
  }

  private static <T, V> void retryOrFail(String methodId,
      long timeout,
      long release,
      ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<V>> resultConsumer,
      AsyncResult<V> event,
      int retryTemp) {
    if (retryTemp < DEFAULT_VALUE) {
      errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer, event);
    } else {
      retry(methodId,
          retryTemp,
          timeout,
          release,
          _step,
          _inputValue,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          vxmsShared,
          resultConsumer,
          event);
    }
  }

  private static <T, V> void executeStateful(String _methodId,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable t,
      Consumer<ExecutionResult<V>> resultConsumer) {
    final Future<V> operationResult = Future.future();
    operationResult.setHandler(event -> {
      if (event.failed()) {
        statefulErrorHandling(_methodId,
            retry,
            timeout,
            circuitBreakerTimeout,
            _step,
            _inputValue,
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

    executeLocked((lock, counter) ->
            counter.get(counterHandler -> {
              long currentVal = counterHandler.result();
              if (currentVal == DEFAULT_LONG_VALUE) {
                executeInitialState(retry,
                    timeout,
                    _step,
                    _inputValue,
                    vxmsShared,
                    operationResult,
                    lock,
                    counter);
              } else if (currentVal > 0) {
                executeDefaultState(timeout, _step,_inputValue, vxmsShared, operationResult, lock);
              } else {
                releaseLockAndHandleError(errorHandler, onFailureRespond, errorMethodHandler,
                    resultConsumer, lock,
                    Optional.ofNullable(t).orElse(Future.failedFuture("circuit open").cause()));
              }
            }), _methodId, vxmsShared, errorHandler, onFailureRespond, errorMethodHandler,
        resultConsumer);


  }


  private static <T> void releaseLockAndHandleError(Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      Consumer<ExecutionResult<T>> resultConsumer,
      Lock lock, Throwable cause) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer,
        Future.failedFuture(cause));
  }

  private static <T,V> void executeDefaultState(long _timeout,
      ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      VxmsShared vxmsShared,
      Future<V> operationResult,
      Lock lock) {
    lock.release();
    if (_timeout > DEFAULT_LONG_VALUE) {
      addTimeoutHandler(_timeout, vxmsShared.getVertx(), (l) -> {
        if (!operationResult.isComplete()) {
          operationResult.fail(new TimeoutException("operation timeout"));
        }
      });
    }
    executeAndCompleate(_step,_inputValue, operationResult);
  }

  private static <T,V> void executeInitialState(int retry,
      long timeout,
      ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      VxmsShared vxmsShared,
      Future<V> operationResult,
      Lock lock,
      Counter counter) {
    final long initialRetryCounterValue = (long) (retry + 1);
    counter.addAndGet(initialRetryCounterValue,
        rHandler -> executeDefaultState(timeout, _step,_inputValue, vxmsShared, operationResult,
            lock));
  }

  private static <T,V> void statefulErrorHandling(String methodId,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<V>> resultConsumer,
      AsyncResult<V> event) {

    executeLocked((lock, counter) ->
            decrementAndExecute(counter, valHandler -> {
              if (valHandler.succeeded()) {
                handleStatefulError(methodId,
                    retry,
                    timeout,
                    circuitBreakerTimeout,
                    _step,
                    _inputValue,
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
                releaseLockAndHandleError(errorHandler, onFailureRespond, errorMethodHandler,
                    resultConsumer, lock, valHandler.cause());
              }
            }), methodId, vxmsShared, errorHandler, onFailureRespond, errorMethodHandler,
        resultConsumer);
  }

  private static void decrementAndExecute(Counter counter,
      Handler<AsyncResult<Long>> asyncResultHandler) {
    counter.decrementAndGet(asyncResultHandler);
  }

  private static <T,V> void handleStatefulError(String methodId,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      ThrowableFutureBiConsumer<T, V> _step,
      T _inputValue,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<V>> resultConsumer,
      AsyncResult<V> event, Lock lock, Counter counter,
      AsyncResult<Long> valHandler) {
    long count = valHandler.result();
    if (count <= DEFAULT_LONG_VALUE) {
      setCircuitBreakerReleaseTimer(retry, circuitBreakerTimeout, vxmsShared.getVertx(), counter);
      openCircuitBreakerAndHandleError(errorHandler, onFailureRespond, errorMethodHandler,
          resultConsumer, event, lock, counter);
    } else {
      lock.release();
      retry(methodId, retry, timeout, circuitBreakerTimeout, _step,_inputValue, errorHandler,
          onFailureRespond, errorMethodHandler, vxmsShared, resultConsumer, event);
    }
  }


  private static <T> void openCircuitBreakerAndHandleError(Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      Consumer<ExecutionResult<T>> resultConsumer,
      AsyncResult<T> event, Lock lock, Counter counter) {
    counter.addAndGet(LOCK_VALUE, val -> {
      lock.release();
      errorHandling(errorHandler, onFailureRespond, errorMethodHandler, resultConsumer,
          Future.failedFuture(event.cause()));
    });
  }

  private static void setCircuitBreakerReleaseTimer(int _retry, long _release, Vertx vertx,
      Counter counter) {
    vertx.setTimer(_release,
        timer -> counter.addAndGet(Integer.valueOf(_retry + 1).longValue(), val -> {
        }));
  }


  private static void addTimeoutHandler(long _timeout, Vertx vertx, Handler<Long> longHandler) {
    vertx.setTimer(_timeout, longHandler);
  }

  private static <T> void errorHandling(Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      Consumer<ExecutionResult<T>> resultConsumer,
      AsyncResult<T> event) {
    try {
      final Future<T> errorResult = Future.future();
      errorResult.setHandler(resultHandler -> {
        if (resultHandler.succeeded()) {
          resultConsumer.accept(new ExecutionResult<>(resultHandler.result(), true, true, null));
        } else {
          handleExecutionError(null, errorHandler, null, errorMethodHandler, resultHandler.cause());
        }
      });
      handleExecutionError(errorResult, errorHandler, onFailureRespond, errorMethodHandler,
          event.cause());

    } catch (Exception e) {
      resultConsumer.accept(new ExecutionResult<>(null, false, e));
    }
  }

  private static <T, V> void retry(String _methodId,
      int retryTemp,
      long timeout,
      long release,
      ThrowableFutureBiConsumer<T, V> step,
      T inputValue,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<V>> resultConsumer,
      AsyncResult<V> event) {
    StepExecution.handleError(errorHandler, event.cause());
    createResponse(_methodId,
        step,
        inputValue,
        errorHandler,
        onFailureRespond,
        errorMethodHandler,
        vxmsShared, null,
        resultConsumer,
        retryTemp,
        timeout,
        release);
  }

  private static <T> void handleExecutionError(Future<T> errorResult,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler, Throwable e) {
    StepExecution.handleError(errorHandler, e);
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


  private static void handleError(Consumer<Throwable> errorHandler, Throwable e) {
    if (errorHandler != null) {
      errorHandler.accept(e);
    }

  }



  private static <T> void executeLocked(LockedConsumer consumer, String methodId,
      VxmsShared vxmsShared, Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler,
      Consumer<ExecutionResult<T>> resultConsumer) {
    // TODO make configurable if cluster wide lock is wanted... than use shared data object instead
    final LocalData localData = vxmsShared.getLocalData();
    localData.getLockWithTimeout(methodId, DEFAULT_LOCK_TIMEOUT, lockHandler -> {
      final Lock lock = lockHandler.result();
      if (lockHandler.succeeded()) {
        localData.getCounter(methodId, resultHandler -> {
          if (resultHandler.succeeded()) {
            consumer.execute(lock, resultHandler.result());
          } else {
            releaseLockAndHandleError(errorHandler, onFailureRespond, errorMethodHandler,
                resultConsumer, lock, resultHandler.cause());
          }
        });
      } else {
        releaseLockAndHandleError(errorHandler, onFailureRespond, errorMethodHandler,
            resultConsumer, lock, lockHandler.cause());
      }

    });
  }


  private interface LockedConsumer {

    void execute(Lock lock, Counter counter);
  }


}
