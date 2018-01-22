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

package org.jacpfx.vxms.event.response.basic;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.List;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.event.interfaces.basic.ExecuteEventbusStringCall;

/**
 * Created by Andy Moncsek on 12.01.16. Fluent API for String responses, defines access to failure
 * handling, timeouts,...
 */
public class ExecuteEventbusStringResponse extends ExecuteEventbusString {

  /**
   * The constructor to pass all needed members
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param message the message to responde to
   * @param chain the execution chain
   * @param stringConsumer the consumer, producing the byte response
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   *     API
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param deliveryOptions the response delivery serverOptions
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteEventbusStringResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      List<ExecutionStep> chain,
      ThrowableFutureConsumer<String> stringConsumer,
      ExecuteEventbusStringCall excecuteEventBusAndReply,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, String> onFailureRespond,
      DeliveryOptions deliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        stringConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  public ExecuteEventbusStringResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      List<ExecutionStep> chain,
      ThrowableFutureConsumer<String> stringConsumer) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        stringConsumer,
        null,
        null,
        null,
        null,
        0,
        0L,
        0L);
  }

  /**
   * intermediate error handler which will be called on each error (at least 1 time, in case on N
   * retries... up to N times)
   *
   * @param errorHandler the handler to be executed on each error
   * @return the response chain {@link ExecuteEventbusStringResponse}
   */
  public ExecuteEventbusStringResponse onError(Consumer<Throwable> errorHandler) {
    return new ExecuteEventbusStringResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        stringConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * Defines how long a method can be executed before aborted.
   *
   * @param timeout the amount of timeout in ms
   * @return the response chain {@link ExecuteEventbusStringResponse}
   */
  public ExecuteEventbusStringResponse timeout(long timeout) {
    return new ExecuteEventbusStringResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        stringConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * retry execution N times before
   *
   * @param retryCount the amount of retries
   * @return the response chain {@link ExecuteEventbusStringCircuitBreaker}
   */
  public ExecuteEventbusStringCircuitBreaker retry(int retryCount) {
    return new ExecuteEventbusStringCircuitBreaker(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        stringConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * defines an action for errors in byte responses, you can handle the error and return an
   * alternate createResponse value, this handler is a terminal handler and will be executed only
   * once
   *
   * @param onFailureRespond the handler (function) to execute on error
   * @return the response chain {@link ExecuteEventbusStringResponse}
   */
  public ExecuteEventbusStringResponse onFailureRespond(
      ThrowableErrorConsumer<Throwable, String> onFailureRespond) {
    return new ExecuteEventbusStringResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        stringConsumer,
        excecuteEventBusAndReply,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }
}
