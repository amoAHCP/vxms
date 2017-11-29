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

package org.jacpfx.vxms.event.response;

import static java.util.Optional.ofNullable;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.ExecutionResult;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.event.response.basic.StepExecution;
import org.jacpfx.vxms.event.response.blocking.ResponseBlockingExecution;

/**
 * The Abstract response defines all common methods used by concrete String, byte and Object
 * responses
 *
 * @param <T> the generic type of the response
 */
public abstract class AbstractResponse<T> {

  /**
   * Executes a step in the supply/andThan chain for non-blocking responses
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param chainList the execution chain
   * @param result the result value of the previous step
   * @param element the current element in step
   * @param step the step
   * @param errorHandler the error handler
   * @param onFailureRespond the result execution on failure
   * @param timeout the timeout time for execution
   * @param circuitBreakerTimeout the time after the circuit breaker will be closed
   * @param retry the amount of retries
   */
  @SuppressWarnings("unchecked")
  protected void executeStep(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      List<ExecutionStep> chainList,
      T result,
      ExecutionStep element,
      ThrowableFutureBiConsumer step,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      long timeout,
      long circuitBreakerTimeout,
      int retry) {
    StepExecution.createResponse(
        methodId,
        retry,
        timeout,
        circuitBreakerTimeout,
        step,
        result,
        errorHandler,
        onFailureRespond,
        errorMethodHandler,
        vxmsShared,
        failure,
        value -> {
          final int index = chainList.indexOf(element);
          final int size = chainList.size();
          if (value.succeeded()) {
            if (index == size - 1 || value.handledError()) {
              // handle last element
              respond(value.getResult());
            } else {
              // call recursive
              final ExecutionStep executionStepAndThan = chainList.get(index + 1);
              ofNullable(executionStepAndThan.getStep())
                  .ifPresent(
                      nextStep ->
                          executeStep(
                              methodId,
                              vxmsShared,
                              failure,
                              errorMethodHandler,
                              chainList,
                              value.getResult(),
                              executionStepAndThan,
                              nextStep,
                              errorHandler,
                              onFailureRespond,
                              timeout,
                              circuitBreakerTimeout,
                              retry));
            }

          } else {
            fail(value.getCause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
          }
        });
  }

  protected void getResultHandler(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      List<ExecutionStep> chainList,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      long timeout,
      long circuitBreakerTimeout,
      int retry,
      ExecutionResult<T> value) {
    if (value.succeeded()) {
      final T result = value.getResult();
      if (chainList.size() > 1 && !value.handledError()) {
        final ExecutionStep executionStepAndThan = chainList.get(1);
        ofNullable(executionStepAndThan.getStep())
            .ifPresent(
                step ->
                    executeStep(
                        methodId,
                        vxmsShared,
                        failure,
                        errorMethodHandler,
                        chainList,
                        result,
                        executionStepAndThan,
                        step,
                        errorHandler,
                        onFailureRespond,
                        timeout,
                        circuitBreakerTimeout,
                        retry));
      } else {
        respond(value.getResult());
      }
    } else {
      fail(value.getCause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
    }
  }

  protected void executeBlocking(
      String methodId,
      ThrowableSupplier<T> supplier,
      Future<ExecutionResult<T>> blockingHandler,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable,T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable failure,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      long delay) {
    ResponseBlockingExecution.createResponseBlocking(
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
        delay);
  }
  @SuppressWarnings("unchecked")
  protected void executeBlockingStep(
      String methodId,
      ThrowableFunction stepNext,
      Object result,
      Future<ExecutionResult<T>> handler,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable,T> onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable failure,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      long delay) {
    org.jacpfx.vxms.event.response.blocking.StepExecution.createResponseBlocking(
        methodId,
        stepNext,
        result,
        handler,
        errorHandler,
        onFailureRespond,
        errorMethodHandler,
        vxmsShared,
        failure,
        retry,
        timeout,
        circuitBreakerTimeout,
        delay);
  }
  @SuppressWarnings("unchecked")
  protected Handler<AsyncResult<ExecutionResult<T>>> getBlockingResultHandler(
      String methodId,
      BlockingExecutionStep step,
      List<BlockingExecutionStep> chainList,
      Consumer<Throwable> errorHandler,
      ThrowableFunction onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      VxmsShared vxmsShared,
      Throwable failure,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      long delay) {
    return value -> {
      if (!value.failed()) {
        ExecutionResult<T> result = value.result();
        if (!result.handledError()) {
          final int index = chainList.indexOf(step);
          final int size = chainList.size();
          if (index == size - 1) {
            // handle last element
            respond(result.getResult());
          } else {
            // call recursive
            final BlockingExecutionStep executionStepAndThan = chainList.get(index + 1);
            final Vertx vertx = vxmsShared.getVertx();
            final T res = result.getResult();
            vertx.executeBlocking(
                handler ->
                    Optional.ofNullable(executionStepAndThan.getStep())
                        .ifPresent(
                            stepNext ->
                                executeBlockingStep(
                                    methodId,
                                    stepNext,
                                    res,
                                    handler,
                                    errorHandler,
                                    onFailureRespond,
                                    errorMethodHandler,
                                    vxmsShared,
                                    failure,
                                    retry,
                                    timeout,
                                    circuitBreakerTimeout,
                                    delay)),
                false,
                getBlockingResultHandler(
                    methodId,
                    executionStepAndThan,
                    chainList,
                    errorHandler,
                    onFailureRespond,
                    errorMethodHandler,
                    vxmsShared,
                    failure,
                    retry,
                    timeout,
                    circuitBreakerTimeout,
                    delay));
          }
        } else {
          respond(result.getResult());
        }
      } else {
        fail(value.cause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
      }
    };
  }

  protected Handler<AsyncResult<ExecutionResult<T>>> getBlockingResultHandler(int retry) {
    return value -> {
      if (!value.failed()) {
        ExecutionResult<T> result = value.result();
        respond(result.getResult());
      } else {
        if (retry == 0) {
          fail(value.cause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        }
      }
    };
  }

  /**
   * The respond method
   *
   * @param result the value to respond
   */
  protected abstract void respond(T result);

  /**
   * The failure method
   *
   * @param result the value to respond
   * @param statuscode the status code
   */
  protected abstract void fail(String result, int statuscode);
}
