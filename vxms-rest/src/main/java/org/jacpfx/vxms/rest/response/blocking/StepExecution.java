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

package org.jacpfx.vxms.rest.response.blocking;

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

/** Created by Andy Moncsek on 01.11.2017 Performs blocking step Executions */
public class StepExecution {

  private static final int DEFAULT_VALUE = 0;
  private static final long DEFAULT_LONG_VALUE = 0;
  private static final int DEFAULT_LOCK_TIMEOUT = 2000;
  private static final int STOP_CONDITION = -1;
  private static final long LOCK_VALUE = -1L;

  /**
   * Creates the response value based on the flow defined in the fluent API. The resulting response
   * will be passed to the resultHandler. This method should be executed in a non blocking context.
   * When defining a timeout a second execution context will be created.
   *
   * @param _methodId the method name/id to be executed
   * @param step the execution step
   * @param value the value from previous step
   * @param _resultHandler the result handler, that takes the result
   * @param _errorHandler the intermediate error method, executed on each error
   * @param _onFailureRespond the method to be executed on failure
   * @param _errorMethodHandler the fallback method
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param _fail last thrown Exception
   * @param _retry the amount of retries
   * @param _timeout, the max timeout time for the method execution
   * @param _circuitBreakerTimeout the stateful circuit breaker release time
   * @param _delay the delay time between retry
   * @param <T> the type of input value
   * @param <V> the type of response
   */
  public static <T, V> void executeRetryAndCatchAsync(
      String _methodId,
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _resultHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, V> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable _fail,
      int _retry,
      long _timeout,
      long _circuitBreakerTimeout,
      long _delay) {
    if (_circuitBreakerTimeout > DEFAULT_LONG_VALUE) {
      executeLocked(
          (lock, counter) ->
              counter.get(
                  counterHandler -> {
                    long currentVal = counterHandler.result();
                    if (currentVal == DEFAULT_LONG_VALUE) {
                      executeInitialState(
                          _methodId,
                          step,
                          value,
                          _resultHandler,
                          _errorHandler,
                          _onFailureRespond,
                          _errorMethodHandler,
                          vxmsShared,
                          _fail,
                          _retry,
                          _timeout,
                          _circuitBreakerTimeout,
                          _delay,
                          lock,
                          counter);
                    } else if (currentVal > DEFAULT_LONG_VALUE) {
                      executeDefault(
                          _methodId,
                          step,
                          value,
                          _resultHandler,
                          _errorHandler,
                          _onFailureRespond,
                          _errorMethodHandler,
                          vxmsShared,
                          _fail,
                          _retry,
                          _timeout,
                          _circuitBreakerTimeout,
                          _delay,
                          lock);
                    } else {
                      executeErrorState(
                          _resultHandler,
                          _errorHandler,
                          _onFailureRespond,
                          _errorMethodHandler,
                          _fail,
                          lock);
                    }
                  }),
          _methodId,
          vxmsShared,
          _resultHandler,
          _errorHandler,
          _onFailureRespond,
          _errorMethodHandler,
          null);
    } else {
      executeStateless(
          step,
          value,
          _resultHandler,
          _errorHandler,
          _onFailureRespond,
          _errorMethodHandler,
          vxmsShared,
          _retry,
          _timeout,
          _delay);
    }
  }

  private static <T> void executeErrorState(
      Future<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Throwable t,
      Lock lock) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    handleErrorExecution(
        _blockingHandler,
        _errorHandler,
        _onFailureRespond,
        _errorMethodHandler,
        Optional.ofNullable(t).orElse(Future.failedFuture("circuit open").cause()));
  }

  private static <T, V> void executeDefault(
      String _methodId,
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _blockingHandler,
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
            executeDefaultState(step, value, _blockingHandler, vxmsShared, _timeout);
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
                                _blockingHandler,
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
                                _blockingHandler,
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
                _blockingHandler,
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
      Future<ExecutionResult<V>> _blockingHandler,
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
                _blockingHandler,
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
      Future<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Throwable cause,
      Lock lock) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    handleErrorExecution(
        _blockingHandler, _errorHandler, _onFailureRespond, _errorMethodHandler, cause);
  }

  private static <T> void handleErrorExecution(
      Future<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Throwable cause) {
    final T result = handleError(_errorHandler, _onFailureRespond, _errorMethodHandler, cause);
    if (!_blockingHandler.isComplete()) {
      _blockingHandler.complete(new ExecutionResult<>(result, true, true, null));
    }
  }

  private static <T, V> void handleStatefulError(
      String _methodId,
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _blockingHandler,
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
    if (count <= DEFAULT_LONG_VALUE) {
      setCircuitBreakerReleaseTimer(vxmsShared, _retry, _circuitBreakerTimeout, counter);
      openCircuitBreakerAndHandleError(
          _blockingHandler,
          _errorHandler,
          _onFailureRespond,
          _errorMethodHandler,
          vxmsShared,
          e,
          lck,
          counter);
    } else {
      lck.release();
      org.jacpfx.vxms.rest.response.basic.ResponseExecution.handleError(_errorHandler, e);
      handleDelay(_delay);
      executeRetryAndCatchAsync(
          _methodId,
          step,
          value,
          _blockingHandler,
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
      Future<ExecutionResult<T>> _blockingHandler,
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
                if (!_blockingHandler.isComplete()) {
                  _blockingHandler.complete(new ExecutionResult<>(result, true, true, null));
                }
              },
              false,
              res -> {});
        });
  }

  private static void setCircuitBreakerReleaseTimer(
      VxmsShared vxmsShared, int _retry, long _circuitBreakerTimeout, Counter counter) {
    final Vertx vertx = vxmsShared.getVertx();
    vertx.setTimer(
        _circuitBreakerTimeout,
        timer -> {
          final long initialRetryCounterValue = (long) (_retry + 1);
          counter.addAndGet(initialRetryCounterValue, val -> {});
        });
  }

  private static <T, V> void executeDefaultState(
      ThrowableFunction<T, V> step,
      T value,
      Future<ExecutionResult<V>> _blockingHandler,
      VxmsShared vxmsShared,
      long _timeout)
      throws Throwable {
    V result;
    if (_timeout > DEFAULT_LONG_VALUE) {
      result = executeWithTimeout(step, value, vxmsShared, _timeout);
    } else {
      result = step.apply(value);
    }
    if (!_blockingHandler.isComplete()) {
      _blockingHandler.complete(new ExecutionResult<>(result, true, false, null));
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
    while (_retry >= DEFAULT_LONG_VALUE) {
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
          org.jacpfx.vxms.rest.response.basic.ResponseExecution.handleError(errorHandler, e);
          handleDelay(delay);
        }
      }
    }
    if (!_blockingHandler.isComplete() && (result!=null||errorHandler==null)) {
      _blockingHandler.complete(new ExecutionResult<>(result, true, errorHandling, null));
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
        errorMethodHandler.accept(e);
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
      Future<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Future<U> blockingCodeHandler) {
    // TODO make cluster-aware
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
                        _blockingHandler,
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
                _blockingHandler,
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
