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

package org.jacpfx.vxms.event.eventbus.blocking;

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
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.event.interfaces.blocking.RecursiveExecutor;
import org.jacpfx.vxms.event.interfaces.blocking.RetryExecutor;
import org.jacpfx.vxms.event.response.basic.ResponseExecution;

/**
 * Created by Andy Moncsek on 05.04.16. Handles event-bus call and blocking execution of the message
 * to create an event-bus response
 */
public class EventbusBridgeExecution {

  private static final long LOCK_VALUE = -1L;
  private static final int DEFAULT_LOCK_TIMEOUT = 2000;
  private static final long NO_TIMEOUT = 0L;

  /**
   * Send event-bus message and process the result in the passed function for blocking execution
   * chain
   *
   * @param methodId the method identifier
   * @param targetId the event-bus target id
   * @param message the message to send
   * @param function the function to process the result message
   * @param deliveryOptions the event-bus delivery serverOptions
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
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   * @param executor the typed executor to process the chain
   * @param retryExecutor the typed retry executor of the chain
   * @param <T> the type of response
   */
  public static <T> void sendMessageAndSupplyHandler(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retryExecutor) {

    if (circuitBreakerTimeout == 0L) {
      executeDefaultState(
          methodId, targetId,
          message,
          function,
          deliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          delay,
          circuitBreakerTimeout,
          executor,
          retryExecutor,
          null);
    } else {
      executeStateful(
          methodId, targetId,
          message,
          function,
          deliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          delay,
          circuitBreakerTimeout,
          executor,
          retryExecutor);
    }
  }

  private static <T> void executeStateful(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> byteFunction,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
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
                        methodId, targetId,
                        message,
                        byteFunction,
                        deliveryOptions,
                        vxmsShared,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        delay,
                        circuitBreakerTimeout,
                        executor,
                        retry,
                        lock,
                        counter);
                  } else if (currentVal > 0) {
                    executeDefaultState(
                        methodId, targetId,
                        message,
                        byteFunction,
                        deliveryOptions,
                        vxmsShared,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        delay,
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
                        delay,
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
        delay,
        circuitBreakerTimeout,
        executor);
  }

  private static <T> void executeInitialState(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> byteFunction,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
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
                methodId, targetId,
                message,
                byteFunction,
                deliveryOptions,
                vxmsShared,
                errorMethodHandler,
                requestMessage,
                encoder,
                errorHandler,
                onFailureRespond,
                responseDeliveryOptions,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout,
                executor,
                retry,
                lock));
  }

  private static <T> void executeDefaultState(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
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
            deliveryOptions,
            event ->
                createSupplierAndExecute(
                    methodId, targetId,
                    message,
                    function,
                    deliveryOptions,
                    vxmsShared,
                    errorMethodHandler,
                    requestMessage,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount,
                    timeout,
                    delay,
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
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
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
        delay,
        circuitBreakerTimeout,
        executor,
        lock,
        cause);
  }

  private static <T> void createSupplierAndExecute(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event) {
    final ThrowableSupplier<T> supplier =
        createSupplier(
            methodId, targetId,
            message,
            function,
            deliveryOptions,
            vxmsShared,
            errorMethodHandler,
            requestMessage,
            encoder,
            errorHandler,
            onFailureRespond,
            responseDeliveryOptions,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout,
            retry,
            event);

    if (circuitBreakerTimeout == NO_TIMEOUT) {
      statelessExecution(
          methodId, targetId,
          message,
          function,
          deliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          delay,
          circuitBreakerTimeout,
          executor,
          retry,
          event,
          supplier);
    } else {
      statefulExecution(
          methodId, targetId,
          message,
          function,
          deliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          delay,
          circuitBreakerTimeout,
          executor,
          retry,
          event,
          supplier);
    }
  }

  private static <T> void statelessExecution(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event,
      ThrowableSupplier<T> byteSupplier) {
    if (event.succeeded() || (event.failed() && retryCount <= 0)) {
      executor.execute(
          methodId,
          vxmsShared,
          event.cause(),
          errorMethodHandler,
          requestMessage,
          byteSupplier,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          delay,
          circuitBreakerTimeout);
    } else if (event.failed() && retryCount > 0) {
      retryFunction(
          methodId, targetId,
          message,
          function,
          deliveryOptions,
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
          delay,
          circuitBreakerTimeout,
          retry);
    }
  }

  private static <T> void statefulExecution(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event,
      ThrowableSupplier<T> supplier) {
    if (event.succeeded()) {
      executor.execute(
          methodId,
          vxmsShared,
          event.cause(),
          errorMethodHandler,
          requestMessage,
          supplier,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          delay,
          circuitBreakerTimeout);
    } else {
      statefulErrorHandling(
          methodId, targetId,
          message,
          function,
          deliveryOptions,
          vxmsShared,
          errorMethodHandler,
          requestMessage,
          encoder,
          errorHandler,
          onFailureRespond,
          responseDeliveryOptions,
          retryCount,
          timeout,
          delay,
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
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event) {

    executeLocked(
        (lock, counter) ->
            counter.decrementAndGet(
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
                          delay,
                          circuitBreakerTimeout,
                          executor,
                          event,
                          lock,
                          counter);
                    } else {
                      lock.release();
                      final Throwable cause = event.cause();
                      retryFunction(
                          methodId, targetId,
                          message,
                          function,
                          deliveryOptions,
                          vxmsShared,
                          cause,
                          errorMethodHandler,
                          requestMessage,
                          encoder,
                          errorHandler,
                          onFailureRespond,
                          responseDeliveryOptions,
                          retryCount,
                          timeout,
                          delay,
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
                        delay,
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
        delay,
        circuitBreakerTimeout,
        executor);
  }

  private static <T> void openCircuitAndHandleError(
      String methodId,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
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
              delay,
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
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RecursiveExecutor<T> executor,
      Lock lock,
      Throwable cause) {
    Optional.ofNullable(lock).ifPresent(Lock::release);
    final ThrowableSupplier<T> failConsumer =
        () -> {
          assert cause != null;
          throw cause;
        };
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
        delay,
        circuitBreakerTimeout);
  }

  private static <T> void retryFunction(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions requestDeliveryOptions,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RetryExecutor<T> retry) {
    ResponseExecution.handleError(errorHandler, failure);
    retry.execute(
        methodId,
        targetId,
        message,
        function,
        requestDeliveryOptions,
        vxmsShared,
        failure,
        errorMethodHandler,
        requestMessage,
        null,
        encoder,
        errorHandler,
        onFailureRespond,
        responseDeliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  private static <T> ThrowableSupplier<T> createSupplier(
      String methodId,
      String targetId,
      Object message,
      ThrowableFunction<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions deliveryOptions,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout,
      RetryExecutor<T> retry,
      AsyncResult<Message<Object>> event) {
    return () -> {
      T resp = null;
      if (event.failed()) {
        if (retryCount > 0) {
          retryFunction(
              methodId, targetId,
              message,
              function,
              deliveryOptions,
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
              delay,
              circuitBreakerTimeout,
              retry);
        } else {
          throw event.cause();
        }
      } else {
        resp = function.apply(event);
      }

      return resp;
    };
  }

  private static <T> void executeLocked(
      LockedConsumer consumer,
      String methodId,
      VxmsShared vxmsShared,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount,
      long timeout,
      long delay,
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
                        delay,
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
                delay,
                circuitBreakerTimeout,
                executor,
                null,
                cause);
          }
        });
  }

  private interface LockedConsumer {

    void execute(Lock lock, Counter counter);
  }
}
