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

package org.jacpfx.vxms.event.response.basic;

import static java.util.Optional.ofNullable;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.List;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;

/**
 * The Abstract response defines all common methods used by concrete String, byte and Object
 * responses
 *
 * @param <T> the generic type of the response
 */
public abstract class AbstractResponse<T> {

  /**
   * Executes a setp in the supply/andThan chain
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
  protected void executeStep(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      List<ExecutionStep> chainList,
      Object result,
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

  /**
   * The respond method
   *
   * @param result the value to respond
   */
  abstract void respond(T result);

  /**
   * The failure method
   *
   * @param result the value to respond
   * @param statuscode the status code
   */
  abstract void fail(String result, int statuscode);
}
