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

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.BlockingExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.event.interfaces.blocking.ExecuteEventbusObjectCall;

/**
 * Created by Andy Moncsek on 12.01.16. Fluent API for byte responses, defines access to failure
 * handling, timeouts,...
 */
public class ExecuteEventbusObjectResponse extends ExecuteEventbusObject {

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
   * @param objectSupplier the supplier, producing the byte response
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   *     API
   * @param encoder the encoder to serialize your object
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param deliveryOptions the response delivery serverOptions
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param delay the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteEventbusObjectResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      List<BlockingExecutionStep> chain,
      ThrowableSupplier<Serializable> objectSupplier,
      ExecuteEventbusObjectCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableFunction<Throwable, Serializable> onFailureRespond,
      DeliveryOptions deliveryOptions,
      int retryCount,
      long timeout,
      long delay,
      long circuitBreakerTimeout) {

    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain, objectSupplier,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

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
   * @param objectSupplier the supplier, producing the byte response
   * @param encoder the encoder to serialize your object
   */
  public ExecuteEventbusObjectResponse(String methodId, VxmsShared vxmsShared, Throwable failure,
      Consumer<Throwable> errorMethodHandler, Message<Object> message,List<BlockingExecutionStep> chain,
      ThrowableSupplier<Serializable> objectSupplier, Encoder encoder) {
    super(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain, objectSupplier,
        null,
        encoder,
        null,
        null,
        null,
        0,
        0L,
        0L,
        0L);
  }

  /**
   * defines an action for errors in byte responses, you can handle the error and return an
   * alternate createResponse value
   *
   * @param onFailureRespond the handler (function) to execute on error
   * @param encoder the encoder to serialize your object
   * @return the createResponse chain {@link ExecuteEventbusObject}
   */
  public ExecuteEventbusObject onFailureRespond(
      ThrowableFunction<Throwable, Serializable> onFailureRespond, Encoder encoder) {
    return new ExecuteEventbusObject(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain, objectSupplier,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * Will be executed on each error
   *
   * @param errorHandler the error handler
   * @return the createResponse chain {@link ExecuteEventbusObjectResponse}
   */
  public ExecuteEventbusObjectResponse onError(Consumer<Throwable> errorHandler) {
    return new ExecuteEventbusObjectResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        objectSupplier,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * retry operation on error
   *
   * @param retryCount the amount of retries before failure execution is triggered
   * @return the createResponse chain {@link ExecuteEventbusObjectCircuitBreaker}
   */
  public ExecuteEventbusObjectCircuitBreaker retry(int retryCount) {
    return new ExecuteEventbusObjectCircuitBreaker(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        objectSupplier,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * Defines how long a method can be executed before aborted.
   *
   * @param timeout time to wait in ms
   * @return the createResponse chain {@link ExecuteEventbusObjectResponse}
   */
  public ExecuteEventbusObjectResponse timeout(long timeout) {
    return new ExecuteEventbusObjectResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        objectSupplier,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }

  /**
   * Defines the delay (in ms) between the createResponse retries (on error).
   *
   * @param delay the delay time in ms before retry
   * @return the createResponse chain {@link ExecuteEventbusObjectResponse}
   */
  public ExecuteEventbusObjectResponse delay(long delay) {
    return new ExecuteEventbusObjectResponse(
        methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        chain,
        objectSupplier,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        delay,
        circuitBreakerTimeout);
  }
}
