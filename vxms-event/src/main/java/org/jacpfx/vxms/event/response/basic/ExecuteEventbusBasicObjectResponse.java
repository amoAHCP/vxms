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

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.io.Serializable;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.event.interfaces.basic.ExecuteEventbusObjectCall;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API for Object responses, defines access to failure handling, timeouts,...
 */
public class ExecuteEventbusBasicObjectResponse extends ExecuteEventbusBasicObject {

  /**
   * The constructor to pass all needed members
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param message the message to responde to
   * @param objectConsumer the consumer, producing the byte response
   * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent
   * API
   * @param encoder the encoder to serialize the response Object
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param deliveryOptions the response delivery options
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the amount of time before the execution will be aborted
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  public ExecuteEventbusBasicObjectResponse(String methodId,
      VxmsShared vxmsShared, Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message,
      ThrowableFutureConsumer<Serializable> objectConsumer,
      ExecuteEventbusObjectCall excecuteEventBusAndReply,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond,
      DeliveryOptions deliveryOptions,
      int retryCount,
      long timeout,
      long circuitBreakerTimeout) {
    super(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        objectConsumer,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }


  /**
   * defines an action for errors in byte responses, you can handle the error and return an
   * alternate createResponse value
   *
   * @param onFailureRespond the handler (function) to execute on error
   * @param encoder the encoder to serialize the response Object
   * @return the createResponse chain {@link ExecuteEventbusBasicObjectResponse}
   */
  public ExecuteEventbusBasicObjectResponse onFailureRespond(
      ThrowableErrorConsumer<Throwable, Serializable> onFailureRespond, Encoder encoder) {
    return new ExecuteEventbusBasicObjectResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        objectConsumer,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }

  /**
   * intermediate error handler which will be called on each error (at least 1 time, in case on N
   * retries... up to N times)
   *
   * @param errorHandler the handler to be executed on each error
   * @return the response chain {@link ExecuteEventbusBasicObjectResponse}
   */
  public ExecuteEventbusBasicObjectResponse onError(Consumer<Throwable> errorHandler) {
    return new ExecuteEventbusBasicObjectResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        objectConsumer,
        excecuteEventBusAndReply,
        encoder,
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
   * @return the response chain {@link ExecuteEventbusBasicObjectResponse}
   */
  public ExecuteEventbusBasicObjectResponse timeout(long timeout) {
    return new ExecuteEventbusBasicObjectResponse(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        objectConsumer,
        excecuteEventBusAndReply,
        encoder,
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
   * @return the response chain {@link ExecuteEventbusBasicObjectCircuitBreaker}
   */
  public ExecuteEventbusBasicObjectCircuitBreaker retry(int retryCount) {
    return new ExecuteEventbusBasicObjectCircuitBreaker(methodId,
        vxmsShared,
        failure,
        errorMethodHandler,
        message,
        objectConsumer,
        excecuteEventBusAndReply,
        encoder,
        errorHandler,
        onFailureRespond,
        deliveryOptions,
        retryCount,
        timeout,
        circuitBreakerTimeout);
  }


}
