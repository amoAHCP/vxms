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

package org.jacpfx.vxms.rest.response.blocking;

import static java.util.Optional.ofNullable;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.ExecutionResult;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.rest.interfaces.blocking.ExecuteEventbusStringCall;

/**
 * Created by Andy Moncsek on 12.01.16. This class is the end of the blocking fluent API, all data
 * collected to execute the chain.
 */
public class ExecuteRSString extends org.jacpfx.vxms.rest.response.basic.ExecuteRSString {

  protected final long delay;
  protected final ExecuteEventbusStringCall excecuteAsyncEventBusAndReply;
  protected final ThrowableSupplier<String> stringSupplier;
  protected final ThrowableFunction<Throwable, String> onFailureRespond;
  protected final List<BlockingExecutionStep> chain;

  /**
   * The constructor to pass all needed members
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param context the vertx routing context
   * @param headers the headers to pass to the response
   * @param stringSupplier the supplier, producing the byte response
   * @param excecuteBlockingEventBusAndReply the response of an event-bus call which is passed to
   *     the fluent API
   * @param encoder the encoder to encode your objects
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param httpStatusCode the http status code to set for response
   * @param httpErrorCode the http error code to set in case of failure handling
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteRSString(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      RoutingContext context,
      Map<String, String> headers,
      ThrowableSupplier<String> stringSupplier,
      List<BlockingExecutionStep> chain,
      ExecuteEventbusStringCall excecuteBlockingEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, String> onFailureRespond,
      int httpStatusCode,
      int httpErrorCode,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        context,
        headers,
        null,
        null,
        null,
        encoder,
        errorHandler,
        null,
        httpStatusCode,
        httpErrorCode,
        retryCount,
        timeout,
        circuitBreakerTimeout);
    this.delay = delay;
    this.excecuteAsyncEventBusAndReply = excecuteBlockingEventBusAndReply;
    this.stringSupplier = stringSupplier;
    this.onFailureRespond = onFailureRespond;
    this.chain = chain;
  }

  @Override
  public void execute(HttpResponseStatus status) {
    Objects.requireNonNull(status);
    new ExecuteRSString(
            methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            context,
            headers,
            stringSupplier,
            chain,
            excecuteAsyncEventBusAndReply,
            encoder,
            errorHandler,
            onFailureRespond,
            status.code(),
            httpErrorCode,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout)
        .execute();
  }

  @Override
  public void execute(HttpResponseStatus status, String contentType) {
    Objects.requireNonNull(status);
    Objects.requireNonNull(contentType);
    new ExecuteRSString(
            methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            context,
            org.jacpfx.vxms.rest.response.basic.ResponseExecution.updateContentType(
                headers, contentType),
            stringSupplier,
            chain,
            excecuteAsyncEventBusAndReply,
            encoder,
            errorHandler,
            onFailureRespond,
            status.code(),
            httpErrorCode,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout)
        .execute();
  }

  @Override
  public void execute(String contentType) {
    Objects.requireNonNull(contentType);
    new ExecuteRSString(
            methodId,
            vxmsShared,
            failure,
            errorMethodHandler,
            context,
            org.jacpfx.vxms.rest.response.basic.ResponseExecution.updateContentType(
                headers, contentType),
            stringSupplier,
            chain,
            excecuteAsyncEventBusAndReply,
            encoder,
            errorHandler,
            onFailureRespond,
            httpStatusCode,
            httpErrorCode,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout)
        .execute();
  }

  @Override
  public void execute() {
    Optional.ofNullable(excecuteAsyncEventBusAndReply)
        .ifPresent(
            evFunction -> {
              try {
                evFunction.execute(
                    vxmsShared,
                    failure,
                    errorMethodHandler,
                    context,
                    headers,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    retryCount,
                    timeout,
                    delay,
                    circuitBreakerTimeout);
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
    Optional.ofNullable(stringSupplier)
        .ifPresent(
            supplier -> {
              int retry = retryCount;
              final Vertx vertx = vxmsShared.getVertx();
              vertx.executeBlocking(
                  handler -> typedExecute(supplier, retry, handler),
                  false,
                  getTypedResultHandler(retry));
            });

    ofNullable(chain)
        .ifPresent(
            (List<BlockingExecutionStep> chainList) -> {
              if (!chainList.isEmpty()) {
                final BlockingExecutionStep executionStep = chainList.get(0);
                ofNullable(executionStep.getChainsupplier())
                    .ifPresent(
                        (initialConsumer) -> {
                          int retry = retryCount;
                          final Vertx vertx = vxmsShared.getVertx();
                          vertx.executeBlocking(
                              handler -> execute(initialConsumer, retry, handler, onFailureRespond),
                              false,
                              getResultHandler(executionStep, chainList, retry));
                        });
              }
            });
  }

  private void typedExecute(
      ThrowableSupplier<String> supplier,
      int retry,
      Future<ExecutionResult<String>> blockingHandler) {
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

  private void execute(
      ThrowableSupplier<Object> supplier,
      int retry,
      Future<ExecutionResult<Object>> blockingHandler,
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

  private <T> Handler<AsyncResult<ExecutionResult<T>>> getResultHandler(
      BlockingExecutionStep step, List<BlockingExecutionStep> chainList, int retry) {
    return value -> {
      if (!value.failed()) {
        ExecutionResult<?> result = value.result();
        if (!result.handledError()) {
          final int index = chainList.indexOf(step);
          final int size = chainList.size();
          if (index == size - 1) {
            // handle last element
            respond((String) result.getResult());
          } else {
            // call recursive
            final BlockingExecutionStep executionStepAndThan = chainList.get(index + 1);
            final Vertx vertx = vxmsShared.getVertx();
            final Object res = result.getResult();
            vertx.executeBlocking(
                handler -> Optional.ofNullable(executionStepAndThan.getStep())
                    .ifPresent(
                        stepNext -> executeStep(retry, handler, res, stepNext, onFailureRespond)),
                false,
                getResultHandler(executionStepAndThan, chainList, retry));
          }
        }
      } else {
        checkAndCloseResponse(retry);
      }
    };
  }

  private <T, V> void executeStep(
      int retry,
      Future<ExecutionResult<V>> handler,
      T res,
      ThrowableFunction stepNext,
      ThrowableFunction onFailureRespond) {
    StepExecution.executeRetryAndCatchAsync(
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

  private Handler<AsyncResult<ExecutionResult<String>>> getTypedResultHandler(int retry) {
    return value -> {
      if (!value.failed()) {
        ExecutionResult<String> result = value.result();
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
}
