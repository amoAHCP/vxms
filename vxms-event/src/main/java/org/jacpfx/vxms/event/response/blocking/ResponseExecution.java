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

package org.jacpfx.vxms.event.response.blocking;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
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
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;

/**
 * Created by Andy Moncsek on 19.01.16. Performs blocking Executions and prepares response
 */
public class ResponseExecution {

  private static final int DEFAULT_VALUE = 0;
  private static final long DEFAULT_LONG_VALUE = 0;
  private static final int DEFAULT_LOCK_TIMEOUT = 2000;
  private static final int STOP_CONDITION = -1;
  private static final long LOCK_VALUE = -1L;

  /**
   * Executes the response creation and handles failures
   *
   * @param methodId the method name/id to be executed
   * @param supplier the user defined supply method to be executed (mapToStringResponse,
   * mapToByteResponse, mapToObjectResponse)
   * @param blockingHandler the result handler, that takes the result
   * @param errorHandler the intermediate error method, executed on each error
   * @param onFailureRespond the method to be executed on failure
   * @param errorMethodHandler the fallback method
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param failure last thrown Exception
   * @param retry the amount of retries
   * @param timeout the max timeout time for the method execution
   * @param circuitBreakerTimeout the stateful circuit breaker release time
   * @param delay the delay time between retry
   * @param <T> the type of response (String, byte, Object)
   */
  public static <T> void createResponseBlocking(
      String methodId,
      ThrowableSupplier<T> supplier,
      Promise<ExecutionResult<T>> blockingHandler,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
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
                          supplier,
                          blockingHandler,
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
                          supplier,
                          blockingHandler,
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
                          blockingHandler,
                          errorHandler,
                          onFailureRespond,
                          errorMethodHandler,
                          failure,
                          lock);
                    }
                  }),
          methodId,
          vxmsShared,
          blockingHandler,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          null);
    } else {
      executeStateless(
          supplier,
          blockingHandler,
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
          Promise<ExecutionResult<T>> _blockingHandler,
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

  private static <T> void executeDefault(
      String _methodId,
      ThrowableSupplier<T> _supplier,
      Promise<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
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
            executeDefaultState(_supplier, _blockingHandler, vxmsShared, _timeout);
            bhandler.complete();
          } catch (Throwable e) {
            executeLocked(
                (lck, counter) ->
                    counter.decrementAndGet(
                        valHandler -> {
                          if (valHandler.succeeded()) {
                            handleStatefulError(
                                _methodId,
                                _supplier,
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
        res -> {
        });
  }

  private static <T> void executeInitialState(
      String _methodId,
      ThrowableSupplier<T> _supplier,
      Promise<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
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
                _supplier,
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
          Promise<ExecutionResult<T>> _blockingHandler,
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
          Promise<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Throwable cause) {
    final T result = handleError(_errorHandler, _onFailureRespond, _errorMethodHandler,
        _blockingHandler, cause);
    if (!_blockingHandler.future().isComplete()) {
      _blockingHandler.complete(new ExecutionResult<>(result, true, true, null));
    }
  }

  private static <T> void handleStatefulError(
      String _methodId,
      ThrowableSupplier<T> _supplier,
      Promise<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
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
      org.jacpfx.vxms.event.response.basic.ResponseExecution.handleError(_errorHandler, e);
      handleDelay(_delay);
      createResponseBlocking(
          _methodId,
          _supplier,
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
          Promise<ExecutionResult<T>> _blockingHandler,
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
                T result = handleError(_errorHandler, _onFailureRespond, _errorMethodHandler,
                    _blockingHandler, e);
                if (!_blockingHandler.future().isComplete()) {
                  _blockingHandler.complete(new ExecutionResult<>(result, true, true, null));
                }
              },
              false,
              res -> {
              });
        });
  }

  private static void setCircuitBreakerReleaseTimer(
      VxmsShared vxmsShared, int _retry, long _circuitBreakerTimeout, Counter counter) {
    final long initialRetryCounterValue = (long) (_retry + 1);
    final Vertx vertx = vxmsShared.getVertx();
    vertx.setTimer(
        _circuitBreakerTimeout, timer -> counter.addAndGet(initialRetryCounterValue, val -> {
        }));
  }

  private static <T> void executeDefaultState(
      ThrowableSupplier<T> _supplier,
      Promise<ExecutionResult<T>> _blockingHandler,
      VxmsShared vxmsShared,
      long _timeout)
      throws Throwable {
    T result;
    if (_timeout > DEFAULT_LONG_VALUE) {
      result = executeWithTimeout(_supplier, vxmsShared, _timeout);
    } else {
      result = _supplier.get();
    }
    if (!_blockingHandler.future().isComplete()) {
      _blockingHandler.complete(new ExecutionResult<>(result, true, false, null));
    }
  }

  private static <T> T executeWithTimeout(
      ThrowableSupplier<T> _supplier, VxmsShared vxmsShared, long _timeout) throws Throwable {
    T result;
    final CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
    final Vertx vertx = vxmsShared.getVertx();
    vertx.executeBlocking(
        (innerHandler) -> {
          try {
            timeoutFuture.complete(_supplier.get());
          } catch (Throwable throwable) {
            timeoutFuture.obtrudeException(throwable);
          }
        },
        false,
        (val) -> {
        });

    try {
      result = timeoutFuture.get(_timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException timeout) {
      throw new TimeoutException("operation _timeout");
    }

    return result;
  }

  private static <T> void executeStateless(
      ThrowableSupplier<T> _supplier,
      Promise<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      int _retry,
      long timeout,
      long delay) {
    T result = null;
    boolean errorHandling = false;
    while (_retry >= DEFAULT_VALUE) {
      errorHandling = false;
      try {
        if (timeout > DEFAULT_LONG_VALUE) {
          result = executeWithTimeout(_supplier, vxmsShared, timeout);
          _retry = STOP_CONDITION;
        } else {
          result = _supplier.get();
          _retry = STOP_CONDITION;
        }

      } catch (Throwable e) {
        _retry--;
        if (_retry < DEFAULT_VALUE) {
          try {
            result = handleError(errorHandler, onFailureRespond, errorMethodHandler,
                _blockingHandler, e);
            errorHandling = true;
          } catch (Exception ee) {
            _blockingHandler.fail(ee);
          }

        } else {
          org.jacpfx.vxms.event.response.basic.ResponseExecution.handleError(errorHandler, e);
          handleDelay(delay);
        }
      }
    }
    if (!_blockingHandler.future().isComplete() && result != null) {
      _blockingHandler.complete(new ExecutionResult<>(result, true, errorHandling, null));
    } else if (!_blockingHandler.future().isComplete()) {
      _blockingHandler.complete(new ExecutionResult<>(result, false, errorHandling, null));
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
      Promise<ExecutionResult<T>> _blockingHandler,
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
            e);
        _blockingHandler.complete();
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
      Promise<ExecutionResult<T>> _blockingHandler,
      Consumer<Throwable> _errorHandler,
      ThrowableFunction<Throwable, T> _onFailureRespond,
      Consumer<Throwable> _errorMethodHandler,
      Promise<U> blockingCodeHandler) {
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
                    Optional.ofNullable(blockingCodeHandler).ifPresent(Promise::complete);
                  }
                });
          } else {
            handleErrorExecution(
                _blockingHandler,
                _errorHandler,
                _onFailureRespond,
                _errorMethodHandler,
                lockHandler.cause());
            Optional.ofNullable(blockingCodeHandler).ifPresent(Promise::complete);
          }
        });
  }

  private interface LockedConsumer {

    void execute(Lock lock, Counter counter);
  }
}
