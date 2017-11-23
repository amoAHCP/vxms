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

package org.jacpfx.vxms.event.response.blocking;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionResult;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.concurrent.LocalData;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.event.response.basic.ResponseExecution;

/** Created by Andy Moncsek on 19.01.16. Performs blocking Executions and prepares response */
public class StepExecution {

  private static final int DEFAULT_VALUE = 0;
  private static final long DEFAULT_LONG_VALUE = 0;
  private static final int DEFAULT_LOCK_TIMEOUT = 2000;
  private static final int STOP_CONDITION = -1;
  private static final long LOCK_VALUE = -1L;

  /**
   * Executes the response creation and handles failures
   *
   * @param methodId the method name/id to be executed
   * @param step the step to execute
   * @param value the return value from the previous step
   * @param resultHandler the result handler, that takes the result
   * @param errorHandler the intermediate error method, executed on each error
   * @param onFailureRespond the method to be executed on failure
   * @param errorMethodHandler the fallback method
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure last thrown Exception
   * @param retry the amount of retries
   * @param timeout the max timeout time for the method execution
   * @param circuitBreakerTimeout the stateful circuit breaker release time
   * @param delay the delay time between retry
   * @param <T> the type of response (String, byte, Object)
   */
  public static <T, V> void createResponseBlocking(
      String methodId,
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> resultHandler,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable failure,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      long delay) {
    if (circuitBreakerTimeout > DEFAULT_LONG_VALUE) {
      executeLocked(
          (lock, counter) ->
              counter.get(
                  counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == DEFAULT_LONG_VALUE) {
                      executeInitialState(
                          methodId,
                          step,
                          value,
                          resultHandler,
                          errorHandler,
                          onFailureRespond,
                          errorMethodHandler,
                          vxmsShared,
                          failure,
                          retry,
                          timeout,
                          circuitBreakerTimeout,
                          delay,
                          lock,
                          counter);
                    } else if (currentVal > DEFAULT_LONG_VALUE) {
                      executeDefault(
                          methodId,
                          step,
                          value,
                          resultHandler,
                          errorHandler,
                          onFailureRespond,
                          errorMethodHandler,
                          vxmsShared,
                          failure,
                          retry,
                          timeout,
                          circuitBreakerTimeout,
                          delay,
                          lock);
                    } else {
                      executeErrorState(
                          resultHandler,
                          errorHandler,
                          onFailureRespond,
                          errorMethodHandler,
                          failure,
                          lock);
                    }
                  }),
          methodId,
          vxmsShared,
          resultHandler,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          null);
    } else {
      executeStateless(
          step,
          value,
          resultHandler,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          vxmsShared,
          retry,
          timeout,
          delay);
    }
  }

  private static <T> void executeErrorState(
      Future<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Throwable failure,
      Lock lock) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    handleErrorExecution(
        _blockingHandler,
        _errorHandler,
        _onFailureRespond,
        _errorMethodHandler,
        Optional.ofNullable(failure).orElse(Future.failedFuture("circuit open").cause()));
  }

  private static <T, V> void executeDefault(
      String _methodId,
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, V> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable _failure,
      int _retry,
      long _timeout,
      long _circuitBreakerTimeout,
      long _delay,
      Lock lock) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    final Vertx vertx = vxmsShared.getVertx();
    vertx.executeBlocking(
        bhandler -> {
          try {
            executeDefaultState(step, value, _resultHandler, vxmsShared, _timeout);
            bhandler.complete();
          } catch (Throwable e) {
            executeLocked(
                (lck, counter) ->
                    counter.decrementAndGet(
                        valHandler -> {
                          if (valHandler.succeeded()) {
                            handleStatefulError(
                                _methodId,
                                step,
                                value,
                                _resultHandler,
                                _errorHandler,
                                _onFailureRespond,
                                _errorMethodHandler,
                                vxmsShared,
                                _failure,
                                _retry,
                                _timeout,
                                _circuitBreakerTimeout,
                                _delay,
                                e,
                                lck,
                                counter,
                                valHandler);
                            bhandler.complete();
                          } else {
                            releaseLockAndHandleError(
                                _resultHandler,
                                _errorHandler,
                                _onFailureRespond,
                                _errorMethodHandler,
                                valHandler.cause(),
                                lck);
                            bhandler.complete();
                          }
                        }),
                _methodId,
                vxmsShared,
                _resultHandler,
                _errorHandler,
                _onFailureRespond,
                _errorMethodHandler,
                bhandler);
          }
        },
        false,
        res -> {});
  }

  private static <T, V> void executeInitialState(
      String _methodId,
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, V> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable _t,
      int _retry,
      long _timeout,
      long _circuitBreakerTimeout,
      long _delay,
      Lock lock,
      Counter counter) {
    final long initialRetryCounterValue = (long) (_retry + 1);
    counter.addAndGet(
        initialRetryCounterValue,
        rHandler ->
            executeDefault(
                _methodId,
                step,
                value,
                _resultHandler,
                _errorHandler,
                _onFailureRespond,
                _errorMethodHandler,
                vxmsShared,
                _t,
                _retry,
                _timeout,
                _circuitBreakerTimeout,
                _delay,
                lock));
  }

  private static <T> void releaseLockAndHandleError(
      Future<ExecutionResult<T>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Throwable cause,
      Lock lock) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    handleErrorExecution(
        _resultHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, cause);
  }

  private static <T> void handleErrorExecution(
      Future<ExecutionResult<T>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Throwable cause) {
    final T result = handleError(_errorHandler, _onFailureRespond, _errorMethodHandler, cause);
    if (!_resultHandler.isComplete()) {
      _resultHandler.complete(new ExecutionResult<>(result, true, true, null));
    }
  }

  private static <T, V> void handleStatefulError(
      String _methodId,
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, V> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable _t,
      int _retry,
      long _timeout,
      long _circuitBreakerTimeout,
      long _delay,
      Throwable e,
      Lock lck,
      Counter counter,
      AsyncResult<Long> valHandler) {
    //////////////////////////////////////////
    long count = valHandler.result();
    if (count <= DEFAULT_VALUE) {
      setCircuitBreakerReleaseTimer(vxmsShared, _retry, _circuitBreakerTimeout, counter);
      openCircuitBreakerAndHandleError(
          _resultHandler,
          _errorHandler,
          _onFailureRespond,
          _errorMethodHandler,
          vxmsShared,
          e,
          lck,
          counter);
    } else {
      lck.release();
      ResponseExecution.handleError(_errorHandler, e);
      handleDelay(_delay);
      createResponseBlocking(
          _methodId,
          step,
          value,
          _resultHandler,
          _errorHandler,
          _onFailureRespond,
          _errorMethodHandler,
          vxmsShared,
          _t,
          _retry,
          _timeout,
          _circuitBreakerTimeout,
          _delay);
    }
    ////////////////////////////////////////
  }

  private static <T> void openCircuitBreakerAndHandleError(
      Future<ExecutionResult<T>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable e,
      Lock lck,
      Counter counter) {
    counter.addAndGet(
        LOCK_VALUE,
        val -> {
          lck.release();
          final Vertx vertx = vxmsShared.getVertx();
          vertx.executeBlocking(
              bhandler -> {
                T result = handleError(_errorHandler, _onFailureRespond, _errorMethodHandler, e);
                if (!_resultHandler.isComplete()) {
                  _resultHandler.complete(new ExecutionResult<>(result, true, true, null));
                }
              },
              false,
              res -> {});
        });
  }

  private static void setCircuitBreakerReleaseTimer(
      VxmsShared vxmsShared, int _retry, long _circuitBreakerTimeout, Counter counter) {
    final long initialRetryCounterValue = (long) (_retry + 1);
    final Vertx vertx = vxmsShared.getVertx();
    vertx.setTimer(
        _circuitBreakerTimeout, timer -> counter.addAndGet(initialRetryCounterValue, val -> {}));
  }

  private static <T, V> void executeDefaultState(
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _resultHandler,
      VxmsShared vxmsShared,
      long _timeout)
      throws Throwable {
    V result;
    if (_timeout > DEFAULT_LONG_VALUE) {
      result = executeWithTimeout(step, value, vxmsShared, _timeout);
    } else {
      result = step.apply(value);
    }
    if (!_resultHandler.isComplete()) {
      _resultHandler.complete(new ExecutionResult<>(result, true, false, null));
    }
  }

  private static <T, V> V executeWithTimeout(
      ThrowableFunction<T, V> step, T value, VxmsShared vxmsShared, long _timeout)
      throws Throwable {
    V result;
    final CompletableFuture<V> timeoutFuture = new CompletableFuture<>();
    final Vertx vertx = vxmsShared.getVertx();
    vertx.executeBlocking(
        (innerHandler) -> {
          try {
            timeoutFuture.complete(step.apply(value));
          } catch (Throwable throwable) {
            timeoutFuture.obtrudeException(throwable);
          }
        },
        false,
        (val) -> {});

    try {
      result = timeoutFuture.get(_timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException timeout) {
      throw new TimeoutException("operation _timeout");
    }

    return result;
  }

  private static <T, V> void executeStateless(
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _blockingHandler,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, V> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      int _retry,
      long timeout,
      long delay) {
    V result = null;
    boolean errorHandling = false;
    while (_retry >= DEFAULT_VALUE) {
      errorHandling = false;
      try {
        if (timeout > DEFAULT_LONG_VALUE) {
          result = executeWithTimeout(step, value, vxmsShared, timeout);
          _retry = STOP_CONDITION;
        } else {
          result = step.apply(value);
          _retry = STOP_CONDITION;
        }

      } catch (Throwable e) {
        _retry--;
        if (_retry < DEFAULT_VALUE) {
          try {
            result = handleError(errorHandler, onFailureRespond, errorMethodHandler, e);
            errorHandling = true;
          } catch (Exception ee) {
            _blockingHandler.fail(ee);
          }

        } else {
          ResponseExecution.handleError(errorHandler, e);
          handleDelay(delay);
        }
      }
    }
    if (!errorHandling || result != null) {
      if (!_blockingHandler.isComplete()) {
        _blockingHandler.complete(new ExecutionResult<>(result, true, errorHandling, null));
      }
    }
  }

  private static void handleDelay(long delay) {
    try {
      if (delay > DEFAULT_LONG_VALUE) {
        Thread.sleep(delay);
      }
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
  }

  private static <T> T handleError(
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      Throwable e) {
    T result = null;
    try {
      if (errorHandler != null) {
        errorHandler.accept(e);
      }
      if (onFailureRespond != null) {
        result = onFailureRespond.apply(e);
      }
      if (errorHandler == null && onFailureRespond == null) {
        errorMethodHandler.accept(
            e); // TODO switch to function to return true if an error method was executed, no if no
        // error method is available
        return null;
      }
    } catch (Throwable throwable) {
      errorMethodHandler.accept(throwable);
    }
    return result;
  }

  private static <T, U> void executeLocked(
      LockedConsumer consumer,
      String _methodId,
      VxmsShared vxmsShared,
      Future<ExecutionResult<T>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Future<U> blockingCodeHandler) {
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
                        _resultHandler,
                        _errorHandler,
                        _onFailureRespond,
                        _errorMethodHandler,
                        resultHandler.cause(),
                        lock);
                    Optional.ofNullable(blockingCodeHandler).ifPresent(Future::complete);
                  }
                });
          } else {
            handleErrorExecution(
                _resultHandler,
                _errorHandler,
                _onFailureRespond,
                _errorMethodHandler,
                lockHandler.cause());
            Optional.ofNullable(blockingCodeHandler).ifPresent(Future::complete);
          }
        });
  }

  private interface LockedConsumer {

    void execute(Lock lock, Counter counter);
  }
}
