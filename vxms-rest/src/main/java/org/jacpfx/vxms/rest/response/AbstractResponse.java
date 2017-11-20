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

package org.jacpfx.vxms.rest.response;

import static java.util.Optional.ofNullable;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jacpfx.vxms.rest.response.basic.StepExecution;
import org.jacpfx.vxms.rest.response.blocking.ResponseExecution;

/**
 * The Abstract response defines all common methods used by concrete String, byte and Object
 * responses
 *
 * @param <T> the generic type of the response
 */
public abstract class AbstractResponse<T> {

  protected void executeBlocking(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Consumer<Throwable> errorHandler,
      ThrowableSupplier<T> supplier,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      long delay,
      Future<ExecutionResult<T>> blockingHandler,
      ThrowableFunction onFailureRespond) {
    ResponseExecution.executeRetryAndCatchAsync(
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

  protected Handler<AsyncResult<ExecutionResult<T>>> getResultHandler(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Consumer<Throwable> errorHandler,
      BlockingExecutionStep step,
      List<BlockingExecutionStep> chainList,
      int httpErrorCode,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      long delay,
      ThrowableFunction onFailureRespond) {
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
            final Object res = result.getResult();
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
                                    onFailureRespond,
                                    errorMethodHandler,
                                    errorHandler,
                                    vxmsShared,
                                    failure,
                                    retry,
                                    timeout,
                                    circuitBreakerTimeout,
                                    delay)),
                false,
                getResultHandler(
                    methodId,
                    vxmsShared,
                    failure,
                    errorMethodHandler,
                    errorHandler,
                    executionStepAndThan,
                    chainList,
                    httpErrorCode,
                    retry,
                    timeout,
                    circuitBreakerTimeout,
                    delay,
                    onFailureRespond));
          }
        } else {
          respond(result.getResult(), httpErrorCode);
        }
      } else {
        checkAndCloseResponse(retry);
      }
    };
  }

  private void executeBlockingStep(
      String methodId,
      ThrowableFunction stepNext,
      Object res,
      Future<ExecutionResult<T>> handler,
      ThrowableFunction onFailureRespond,
      Consumer<Throwable> errorMethodHandler,
      Consumer<Throwable> errorHandler,
      VxmsShared vxmsShared,
      Throwable failure,
      int retry,
      long timeout,
      long circuitBreakerTimeout,
      long delay) {

    org.jacpfx.vxms.rest.response.blocking.StepExecution.executeRetryAndCatchAsync(
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
        delay);
  }

  protected abstract void checkAndCloseResponse(int retry);

  protected Handler<AsyncResult<ExecutionResult<T>>> getResultHandler(int retry, int httpErrorCode) {
    return value -> {
      if (!value.failed()) {
        ExecutionResult<T> result = value.result();
        if (!result.handledError()) {
          respond(result.getResult());
        } else {
          respond(result.getResult(), httpErrorCode);
        }
      } else {
        checkAndCloseResponse(retry);
      }
    };
  }

  protected void executeStep(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      List<ExecutionStep> chainList,
      int retry,
      int httpErrorCode,
      long timeout,
      long circuitBreakerTimeout,
      Object result,
      ExecutionStep element,
      ThrowableFutureBiConsumer step) {
    StepExecution.createResponse(
        methodId,
        step,
        result,
        errorHandler,
        onFailureRespond,
        errorMethodHandler,
        vxmsShared,
        failure,
        v -> {
          final int index = chainList.indexOf(element);
          final int size = chainList.size();
          if (v.succeeded()) {
            if (!v.handledError()) {
              if (index == size - 1) {
                // handle last element
                respond(v.getResult());
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
                                errorHandler,
                                onFailureRespond,
                                chainList,
                                retry,
                                httpErrorCode,
                                timeout,
                                circuitBreakerTimeout,
                                v.getResult(),
                                executionStepAndThan,
                                nextStep));
              }
            } else {
              respond(v.getResult(), httpErrorCode);
            }
          } else {
            errorRespond(
                v.getCause().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
          }
        },
        retry,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * The respond method
   *
   * @param result the value to respond
   */
  protected abstract void respond(T result);

  /**
   * The respond method
   *
   * @param result the value to respond
   * @param statusCode the http status code
   */
  protected abstract void respond(T result, int statusCode);

  /**
   * The error respond method
   *
   * @param result the value to respond
   * @param statusCode the http status code
   */
  protected abstract void errorRespond(String result, int statusCode);

  /**
   * Add a http content type to request header
   *
   * @param header the current header map
   * @param contentType the content type to add
   * @return as new header map instance
   */
  protected Map<String, String> updateContentType(Map<String, String> header,
      String contentType) {
    Map<String, String> headerMap = new HashMap<>(header);
    headerMap.put("content-type", contentType);
    return headerMap;
  }
}
