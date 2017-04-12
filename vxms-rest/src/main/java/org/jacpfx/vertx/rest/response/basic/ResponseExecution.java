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

package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.jacpfx.common.ExecutionResult;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.concurrent.LocalData;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.common.throwable.ThrowableFutureConsumer;

/**
 * Created by Andy Moncsek on 21.07.16. Contains the method chain to execute the response chaine
 * defined in the fluent API. The class is generic, so it is used for all types of response.
 */
public class ResponseExecution {

  private static final int DEFAULT_VALUE = 0;
  private static final long DEFAULT_LONG_VALUE = 0l;
  private static final int DEFAULT_LOCK_TIMEOUT = 2000;
  private static final long LOCK_VALUE = -1l;

  /**
   * Creates the response value based on the flow defined in the fluent API.  The resulting response
   * will be passed to an execution consumer.
   *
   * @param methodId, the method name/id to be executed
   * @param retry, the amount of retries
   * @param timeout, the timeout time for execution
   * @param circuitBreakerTimeout, the stateful circuit breaker release time
   * @param userOperation, the user operation to be executed (mapToStringResponse,
   * mapToByteResponse, mapToObjectResponse)
   * @param errorHandler, the intermediate error method, executed on each error
   * @param onFailureRespond, the method to be executed on failure
   * @param errorMethodHandler, the fallback method
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param fail, last thrown Exception
   * @param resultConsumer, the consumer of the {@link ExecutionResult}
   * @param <T> the type of response (String, byte, Object)
   */
  public static <T> void createResponse(String methodId,
      ThrowableFutureConsumer<T> userOperation,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable fail,
      Consumer<ExecutionResult<T>> resultConsumer,
      int retry,
      long timeout,
      long circuitBreakerTimeout) {

    if (circuitBreakerTimeout > DEFAULT_LONG_VALUE) {
      executeStateful(methodId,
          retry, timeout,
          circuitBreakerTimeout,
          userOperation,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          vxmsShared, fail, resultConsumer);
    } else {
      executeStateless(methodId,
          retry,
          timeout,
          circuitBreakerTimeout,
          userOperation,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          vxmsShared, resultConsumer);
    }
  }

  private static <T> void executeStateless(String _methodId,
      int retry,
      long timeout,
      long release,
      ThrowableFutureConsumer<T> _userOperation,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<T>> resultConsumer) {
    final Future<T> operationResult = Future.future();
    operationResult.setHandler(event -> {
      if (event.failed()) {
        int retryTemp = retry - 1;
        retryOrFail(_methodId,
            timeout,
            release,
            _userOperation,
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
    executeAndCompleate(_userOperation, operationResult);


  }

  private static <T> void executeAndCompleate(ThrowableFutureConsumer<T> userOperation,
      Future<T> operationResult) {

    try {
      userOperation.accept(operationResult);
    } catch (Throwable throwable) {
      operationResult.fail(throwable);
    }
  }

  private static <T> void retryOrFail(String methodId,
      long timeout,
      long release,
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
      retry(methodId,
          retryTemp,
          timeout,
          release,
          _userOperation,
          errorHandler,
          onFailureRespond,
          errorMethodHandler,
          vxmsShared,
          resultConsumer,
          event);
    }
  }

  private static <T> void executeStateful(String _methodId,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      ThrowableFutureConsumer<T> _userOperation,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable t,
      Consumer<ExecutionResult<T>> resultConsumer) {
    final Future<T> operationResult = Future.future();
    operationResult.setHandler(event -> {
      if (event.failed()) {
        statefulErrorHandling(_methodId,
            retry,
            timeout,
            circuitBreakerTimeout,
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

    executeLocked((lock, counter) ->
            counter.get(counterHandler -> {
              long currentVal = counterHandler.result();
              if (currentVal == DEFAULT_LONG_VALUE) {
                executeInitialState(retry,
                    timeout,
                    _userOperation,
                    vxmsShared,
                    operationResult,
                    lock,
                    counter);
              } else if (currentVal > 0) {
                executeDefaultState(timeout, _userOperation, vxmsShared, operationResult, lock);
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

  private static <T> void executeDefaultState(long _timeout,
      ThrowableFutureConsumer<T> _userOperation, VxmsShared vxmsShared, Future<T> operationResult,
      Lock lock) {
    lock.release();
    if (_timeout > DEFAULT_LONG_VALUE) {
      addTimeoutHandler(_timeout, vxmsShared.getVertx(), (l) -> {
        if (!operationResult.isComplete()) {
          operationResult.fail(new TimeoutException("operation timeout"));
        }
      });
    }
    executeAndCompleate(_userOperation, operationResult);
  }

  private static <T> void executeInitialState(int retry,
      long timeout,
      ThrowableFutureConsumer<T> _userOperation,
      VxmsShared vxmsShared,
      Future<T> operationResult,
      Lock lock,
      Counter counter) {
    final long initialRetryCounterValue = (long) (retry + 1);
    counter.addAndGet(initialRetryCounterValue,
        rHandler -> executeDefaultState(timeout, _userOperation, vxmsShared, operationResult,
            lock));
  }

  private static <T> void statefulErrorHandling(String methodId,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      ThrowableFutureConsumer<T> _userOperation,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<T>> resultConsumer,
      AsyncResult<T> event) {

    executeLocked((lock, counter) ->
            decrementAndExecute(counter, valHandler -> {
              if (valHandler.succeeded()) {
                handleStatefulError(methodId,
                    retry,
                    timeout,
                    circuitBreakerTimeout,
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

  private static <T> void handleStatefulError(String methodId,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      ThrowableFutureConsumer<T> _userOperation,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<T>> resultConsumer,
      AsyncResult<T> event, Lock lock, Counter counter,
      AsyncResult<Long> valHandler) {
    long count = valHandler.result();
    if (count <= DEFAULT_LONG_VALUE) {
      setCircuitBreakerReleaseTimer(retry, circuitBreakerTimeout, vxmsShared.getVertx(), counter);
      openCircuitBreakerAndHandleError(errorHandler, onFailureRespond, errorMethodHandler,
          resultConsumer, event, lock, counter);
    } else {
      lock.release();
      retry(methodId, retry, timeout, circuitBreakerTimeout, _userOperation, errorHandler,
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

  private static <T> void retry(String _methodId,
      int retryTemp,
      long timeout,
      long release,
      ThrowableFutureConsumer<T> userOperation,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Consumer<ExecutionResult<T>> resultConsumer,
      AsyncResult<T> event) {
    ResponseExecution.handleError(errorHandler, event.cause());
    createResponse(_methodId,
        userOperation,
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
    ResponseExecution.handleError(errorHandler, e);
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


  /**
   * checks errorHandler for null and passes the throwable
   *
   * @param errorHandler The error handler that's accepts a Throwable
   * @param e, the Throwable
   */
  public static void handleError(Consumer<Throwable> errorHandler, Throwable e) {
    if (errorHandler != null) {
      errorHandler.accept(e);
    }

  }

  /**
   * Add a http content type to request header
   *
   * @param header the current header map
   * @param contentType the content type to add
   * @return as new header map instance
   */
  public static Map<String, String> updateContentType(Map<String, String> header,
      String contentType) {
    Map<String, String> headerMap = new HashMap<>(header);
    headerMap.put("content-type", contentType);
    return headerMap;
  }

  /**
   * Checks the type of result and reply response
   *
   * @param val, the value to response
   * @param handler, the server response
   */
  public static void sendObjectResult(Object val, HttpServerResponse handler) {
    if (val instanceof String) {
      handler.end(String.valueOf(val));
    } else {
      handler.end(Buffer.buffer((byte[]) val));
    }
  }

  /**
   * Perform http status code and header update on http response
   *
   * @param headers the header to set
   * @param statusCode, the http code to set
   * @param response, the http response object
   */
  public static void updateHeaderAndStatuscode(Map<String, String> headers, int statusCode,
      HttpServerResponse response) {
    updateResponseHaders(headers, response);
    updateResponseStatusCode(statusCode, response);
  }

  private static void updateResponseHaders(Map<String, String> headers,
      HttpServerResponse response) {
    Optional.ofNullable(headers).ifPresent(
        h -> h.entrySet().forEach(entry -> response.putHeader(entry.getKey(), entry.getValue())));
  }

  private static void updateResponseStatusCode(int httpStatusCode, HttpServerResponse response) {
    if (httpStatusCode != 0) {
      response.setStatusCode(httpStatusCode);
    }
  }

  /**
   * Apply the {@link Encoder} to a provided {@link Serializable}
   *
   * @param value, the value to encode
   * @param encoder the {@link Encoder}
   * @return an Optional with the encoded value
   */
  @SuppressWarnings("unchecked")
  public static Optional<?> encode(Serializable value, Encoder encoder) {
    try {
      if (encoder instanceof Encoder.ByteEncoder) {
        return Optional.ofNullable(((Encoder.ByteEncoder) encoder).encode(value));
      } else if (encoder instanceof Encoder.StringEncoder) {
        return Optional.ofNullable(((Encoder.StringEncoder) encoder).encode(value));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private static <T> void executeLocked(LockedConsumer consumer, String methodId,
      VxmsShared vxmsShared, Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler,
      Consumer<ExecutionResult<T>> resultConsumer) {
    // final SharedData sharedData = vxmsShared.getVertx().sharedData();
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
