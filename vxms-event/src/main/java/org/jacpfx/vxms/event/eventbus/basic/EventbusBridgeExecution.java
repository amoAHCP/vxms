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

package org.jacpfx.vxms.event.eventbus.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.concurrent.LocalData;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.event.interfaces.basic.RecursiveExecutor;
import org.jacpfx.vxms.event.interfaces.basic.RetryExecutor;
import org.jacpfx.vxms.event.response.basic.ResponseExecution;

/**
 * Created by Andy Moncsek on 05.04.16. Handles event-bus call and non-blocking execution of the
 * message to create an event-bus response
 */
public class EventbusBridgeExecution {

  private static final long LOCK_VALUE = -1L;
  private static final int DEFAULT_LOCK_TIMEOUT = 2000;
  private static final long NO_TIMEOUT = 0L;

  /**
   * Send event-bus message and process the result in the passed function for the execution chain
   *
   * @param methodId the method identifier
   * @param targetId the event-bus target id
   * @param message the message to send
   * @param function the function to process the result message
   * @param requestDeliveryOptions the event-bus delivery serverOptions
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param errorMethodHandler the error-method handler
   * @param requestMessage the request message to respond after chain execution
   * @param encoder the encoder to serialize the response object
   * @param errorHandler the error handler
   * @param onFailureRespond the function that takes a Future with the alternate response value in
   *     case of failure
   * @param responseDeliveryOptions the delivery serverOptions for the event response
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   * @param executor the typed executor to process the chain
   * @param retryExecutor the typed retry executor of the chain
   * @param <T> the type of response
   */
  public static <T> void sendMessageAndSupplyHandler(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retryExecutor) {

    if (circuitBreakerTimeout == 0) {
      executeDefaultState(
          methodId,
          targetId,
          message,
          function,
          requestDeliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout,
          executor,
          retryExecutor,
          null);

    } else {
      executeStateful(
          methodId,
          targetId,
          message,
          function,
          requestDeliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout,
          executor,
          retryExecutor);
    }
  }

  private static <T> void executeStateful(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry) {

    executeLocked(
        ((lock, counter) ->
            counter.get(
                counterHandler -> {
                  long currentVal = counterHandler.result();
                  if (currentVal == 0) {
                    executeInitialState(
                        methodId,
                        targetId,
                        message,
                        objectFunction,
                        requestDeliveryOptions,
                        vxmsShared,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout,
                        executor,
                        retry,
                        lock,
                        counter);
                  } else if (currentVal > 0) {
                    executeDefaultState(
                        methodId,
                        targetId,
                        message,
                        objectFunction,
                        requestDeliveryOptions,
                        vxmsShared,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout,
                        executor,
                        retry,
                        lock);
                  } else {
                    executeErrorState(
                        methodId,
                        vxmsShared,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout,
                        executor,
                        lock);
                  }
                })),
        methodId,
        vxmsShared,
        errorMethodHandler,
        requestMessage,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout,
        executor);
  }

  private static <T> void executeInitialState(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      Lock lock,
      Counter counter) {
    int incrementCounter = retryCount + 1;
    counter.addAndGet(
        Integer.valueOf(incrementCounter).longValue(),
        rHandler ->
            executeDefaultState(
                methodId,
                targetId,
                message,
                objectFunction,
                requestDeliveryOptions,
                vxmsShared,
                errorMethodHandler,
                requestMessage,
                encoder,
                errorHandler,
                onFailureRespond,
                responseDeliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout,
                executor,
                retry,
                lock));
  }

  private static <T> void executeDefaultState(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      Lock lock) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    final Vertx vertx = vxmsShared.getVertx();
    vertx
        .eventBus()
        .request(
            targetId,
            message,
            requestDeliveryOptions,
            event ->
                createSupplierAndExecute(
                    methodId,
                    targetId,
                    message,
                    objectFunction,
                    requestDeliveryOptions,
                    vxmsShared,
                    errorMethodHandler,
                    requestMessage,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount,
                    timeout,
                    circuitBreakerTimeout,
                    executor,
                    retry,
                    event));
  }

  private static <T> void executeErrorState(
      String methodId,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      Lock lock) {
    final Throwable cause = Future.failedFuture("circuit open").cause();
    handleError(
        methodId,
        vxmsShared,
        errorMethodHandler,
        requestMessage,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout,
        executor,
        lock,
        cause);
  }

  private static <T> void createSupplierAndExecute(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event) {
    final ThrowableFutureConsumer<T> objectConsumer = createSupplier(objectFunction, event);
    if (circuitBreakerTimeout == NO_TIMEOUT) {
      statelessExecution(
          methodId,
          targetId,
          message,
          objectFunction,
          requestDeliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout,
          executor,
          retry,
          event,
          objectConsumer);
    } else {
      statefulExecution(
          methodId,
          targetId,
          message,
          objectFunction,
          requestDeliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout,
          executor,
          retry,
          event,
          objectConsumer);
    }
  }

  private static <T> void statelessExecution(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event,
      ThrowableFutureConsumer<T> objectConsumer) {
    if (!event.failed() || (event.failed() && retryCount <= 0)) {
      executor.execute(
          methodId,
          vxmsShared,
          event.cause(),
          errorMethodHandler,
          requestMessage,
          objectConsumer,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout);
    } else if (event.failed() && retryCount > 0) {
      retryFunction(
          methodId, targetId,
          message,
          objectFunction,
          requestDeliveryOptions,
          vxmsShared,
          event.cause(),
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout,
          retry);
    }
  }

  private static <T> void statefulExecution(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event,
      ThrowableFutureConsumer<T> objectConsumer) {
    if (event.succeeded()) {
      executor.execute(
          methodId,
          vxmsShared,
          event.cause(),
          errorMethodHandler,
          requestMessage,
          objectConsumer,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout);
    } else {
      statefulErrorHandling(
          methodId,
          targetId,
          message,
          objectFunction,
          requestDeliveryOptions,
          vxmsShared,
          event.cause(),
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          circuitBreakerTimeout,
          executor,
          retry,
          event);
    }
  }

  private static <T> void statefulErrorHandling(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Throwable t,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event) {

    executeLocked(
        (lock, counter) ->
            decrementAndExecute(
                counter,
                valHandler -> {
                  if (valHandler.succeeded()) {
                    long count = valHandler.result();
                    if (count <= 0) {
                      openCircuitAndHandleError(
                          methodId,
                          vxmsShared,
                          errorMethodHandler,
                          requestMessage,
                          encoder,
                          errorHandler,
                          onFailureRespond,
                          responseDeliveryOptions,
                          retryCount,
                          timeout,
                          circuitBreakerTimeout,
                          executor,
                          event,
                          lock,
                          counter);
                    } else {
                      lock.release();
                      retryFunction(
                          methodId, targetId,
                          message,
                          objectFunction,
                          requestDeliveryOptions,
                          vxmsShared,
                          t,
                          errorMethodHandler,
                          requestMessage,
                          encoder,
                          errorHandler,
                          onFailureRespond,
                          responseDeliveryOptions,
                          retryCount,
                          timeout,
                          circuitBreakerTimeout,
                          retry);
                    }
                  } else {
                    final Throwable cause = valHandler.cause();
                    handleError(
                        methodId,
                        vxmsShared,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout,
                        executor,
                        lock,
                        cause);
                  }
                }),
        methodId,
        vxmsShared,
        errorMethodHandler,
        requestMessage,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout,
        executor);
  }

  private static void decrementAndExecute(
      Counter counter, Handler<AsyncResult<Long>> asyncResultHandler) {
    counter.decrementAndGet(asyncResultHandler);
  }

  private static <T> void executeLocked(
      LockedConsumer consumer,
      String methodId,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor) {
    final LocalData sharedData = vxmsShared.getLocalData();
    sharedData.getLockWithTimeout(
        methodId,
        DEFAULT_LOCK_TIMEOUT,
        lockHandler -> {
          if (lockHandler.succeeded()) {
            final Lock lock = lockHandler.result();
            sharedData.getCounter(
                methodId,
                resultHandler -> {
                  if (resultHandler.succeeded()) {
                    consumer.execute(lock, resultHandler.result());
                  } else {
                    final Throwable cause = resultHandler.cause();
                    handleError(
                        methodId,
                        vxmsShared,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout,
                        executor,
                        lock,
                        cause);
                  }
                });
          } else {
            final Throwable cause = lockHandler.cause();
            handleError(
                methodId,
                vxmsShared,
                errorMethodHandler,
                requestMessage,
                encoder,
                errorHandler,
                onFailureRespond,
                responseDeliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout,
                executor,
                null,
                cause);
          }
        });
  }

  private static <T> void openCircuitAndHandleError(
      String methodId,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      AsyncResult<Message<Object>> event,
      Lock lock,
      Counter counter) {
    resetLockTimer(vxmsShared, retryCount, circuitBreakerTimeout, counter);
    lockAndHandle(
        counter,
        val -> {
          final Throwable cause = event.cause();
          handleError(
              methodId,
              vxmsShared,
              errorMethodHandler,
              requestMessage,
              encoder,
              errorHandler,
              onFailureRespond,
              responseDeliveryOptions,
              retryCount,
              timeout,
              circuitBreakerTimeout,
              executor,
              lock,
              cause);
        });
  }

  private static void lockAndHandle(
      Counter counter, Handler<AsyncResult<Long>> asyncResultHandler) {
    counter.addAndGet(LOCK_VALUE, asyncResultHandler);
  }

  private static void resetLockTimer(
      VxmsShared vxmsShared, int retryCount, long circuitBreakerTimeout, Counter counter) {
    final Vertx vertx = vxmsShared.getVertx();
    vertx.setTimer(
        circuitBreakerTimeout,
        timer -> counter.addAndGet(Integer.valueOf(retryCount + 1).longValue(), val -> {}));
  }

  private static <T> void handleError(
      String methodId,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      Lock lock,
      Throwable cause) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    final ThrowableFutureConsumer<T> failConsumer = (future) -> future.fail(cause);
    executor.execute(
        methodId,
        vxmsShared,
        cause,
        errorMethodHandler,
        requestMessage,
        failConsumer,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  private static <T> void retryFunction(
      String methodId,
      String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Throwable t,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout,
      RetryExecutor<T> retry) {
    ResponseExecution.handleError(errorHandler, t);
    retry.execute(
        targetId,
        methodId,
        message,
        objectFunction,
        requestDeliveryOptions,
        vxmsShared,
        t,
        errorMethodHandler,
        requestMessage,
        null,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  private static <T> ThrowableFutureConsumer<T> createSupplier(
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> objectFunction,
      AsyncResult<Message<Object>> event) {
    return (future) -> {
      if (event.failed()) {
        future.fail(event.cause());
      } else {
        objectFunction.accept(event, future);
      }
    };
  }

  private interface LockedConsumer {

    void execute(Lock lock, Counter counter);
  }
}
